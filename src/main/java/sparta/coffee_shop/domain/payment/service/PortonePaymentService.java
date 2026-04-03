package sparta.coffee_shop.domain.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sparta.coffee_shop.common.exception.ErrorEnum;
import sparta.coffee_shop.common.exception.ServiceErrorException;
import sparta.coffee_shop.common.portone.PortoneClient;
import sparta.coffee_shop.common.portone.PortonePaymentResult;
import sparta.coffee_shop.domain.order.entity.Order;
import sparta.coffee_shop.domain.order.entity.OrderStatus;
import sparta.coffee_shop.domain.order.event.OrderCompletedEvent;
import sparta.coffee_shop.domain.orderlog.entity.OrderLog;
import sparta.coffee_shop.domain.orderlog.repository.OrderLogRepository;
import sparta.coffee_shop.domain.payment.dto.request.PortonePaymentRequest;
import sparta.coffee_shop.domain.payment.dto.request.PortoneWebhookRequest;
import sparta.coffee_shop.domain.payment.dto.response.PortonePaymentResponse;
import sparta.coffee_shop.domain.payment.entity.Payment;
import sparta.coffee_shop.domain.payment.entity.PaymentStatus;
import sparta.coffee_shop.domain.payment.entity.PaymentType;
import sparta.coffee_shop.domain.payment.repository.PaymentRepository;
import sparta.coffee_shop.domain.paymentLog.entity.PaymentLog;
import sparta.coffee_shop.domain.paymentLog.repository.PaymentLogRepository;

import java.time.Duration;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortonePaymentService {

    private static final String POPULAR_KEY_PREFIX = "coffee:popular:";

    private final PaymentRepository paymentRepository;
    private final PaymentLogRepository paymentLogRepository;
    private final OrderLogRepository orderLogRepository;
    private final PortoneClient portoneClient;
    private final ApplicationEventPublisher eventPublisher;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public PortonePaymentResponse confirmPayment(PortonePaymentRequest request) {
        // 1. merchantUid로 Payment 조회
        Payment payment = paymentRepository.findByMerchantUid(request.getMerchantUid())
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_PAYMENT_NOT_FOUND));

        // 2. 멱등성 검증 - 이미 처리된 결제면 중단
        if (payment.getStatus() != PaymentStatus.PENDING) {
            log.warn("[중복 결제 요청] merchantUid={}, status={}", request.getMerchantUid(), payment.getStatus());
            throw new ServiceErrorException(ErrorEnum.ERR_PAYMENT_ALREADY_PROCESSED);
        }

        // 3. 포트원 결제 단건 조회 (서버 검증)
        PortonePaymentResult portoneResult = portoneClient.getPayment(request.getPortonePaymentId());

        if (portoneResult == null || !portoneResult.isPaid()) {
            payment.fail();
            updateOrderStatus(payment.getOrder(), OrderStatus.PAYMENT_PENDING, OrderStatus.FAILED, "포트원 결제 미완료");
            paymentLogRepository.save(new PaymentLog(
                    payment, PaymentStatus.PENDING, PaymentStatus.FAIL,
                    PaymentType.PORTONE, request.getPortonePaymentId(),
                    0, "포트원 결제 미완료"
            ));
            throw new ServiceErrorException(ErrorEnum.ERR_PAYMENT_PORTONE_FAILED);
        }

        // 4. 금액 검증 (위변조 방지)
        if (portoneResult.getPaidAmount() != payment.getTotalPrice()) {
            payment.fail();
            updateOrderStatus(payment.getOrder(), OrderStatus.PAYMENT_PENDING, OrderStatus.FAILED, "결제 금액 불일치");
            paymentLogRepository.save(new PaymentLog(
                    payment, PaymentStatus.PENDING, PaymentStatus.FAIL,
                    PaymentType.PORTONE, request.getPortonePaymentId(),
                    portoneResult.getPaidAmount(), "결제 금액 불일치"
            ));
            log.error("[금액 불일치] expected={}, actual={}", payment.getTotalPrice(), portoneResult.getPaidAmount());
            throw new ServiceErrorException(ErrorEnum.ERR_PAYMENT_AMOUNT_MISMATCH);
        }

        // 5. 결제 확인 처리
        payment.confirm(request.getPortonePaymentId());
        updateOrderStatus(payment.getOrder(), OrderStatus.PAYMENT_PENDING, OrderStatus.PAID, null);
        paymentLogRepository.save(new PaymentLog(
                payment, PaymentStatus.PENDING, PaymentStatus.SUCCESS,
                PaymentType.PORTONE, request.getPortonePaymentId(),
                portoneResult.getPaidAmount(), null
        ));

        // 6. Redis 인기 메뉴 점수 증가
        Order order = payment.getOrder();
        updatePopularMenu(order.getMenu().getId());

        // 7. 데이터 플랫폼 이벤트 발행
        eventPublisher.publishEvent(new OrderCompletedEvent(
                order.getUser().getUserKey(), order.getMenu().getId(), payment.getTotalPrice()
        ));

        log.info("[포트원 결제 확인 완료] merchantUid={}, portonePaymentId={}",
                request.getMerchantUid(), request.getPortonePaymentId());
        return PortonePaymentResponse.from(payment);
    }

    @Transactional
    public void handleWebhook(PortoneWebhookRequest request) {
        String portonePaymentId = request.getData() != null ? request.getData().getPaymentId() : null;

        if (portonePaymentId == null) {
            log.warn("[웹훅 무시] paymentId 없음");
            return;
        }

        if (paymentRepository.existsByPortonePaymentId(portonePaymentId)) {
            log.info("[웹훅 중복 수신 무시] portonePaymentId={}", portonePaymentId);
            return;
        }

        if (!"Transaction.Paid".equals(request.getType())) {
            log.info("[웹훅 무시] type={}", request.getType());
            return;
        }

        PortonePaymentResult portoneResult = portoneClient.getPayment(portonePaymentId);

        if (portoneResult == null || !portoneResult.isPaid()) {
            log.warn("[웹훅 결제 미완료] portonePaymentId={}", portonePaymentId);
            return;
        }

        String merchantUid = portoneResult.getCustomData();
        if (merchantUid == null) {
            log.warn("[웹훅 merchantUid 없음] portonePaymentId={}", portonePaymentId);
            return;
        }

        Payment payment = paymentRepository.findByMerchantUid(merchantUid).orElse(null);
        if (payment == null) {
            log.warn("[웹훅 Payment 없음] merchantUid={}", merchantUid);
            return;
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            log.info("[웹훅 이미 처리됨] merchantUid={}, status={}", merchantUid, payment.getStatus());
            return;
        }

        if (portoneResult.getPaidAmount() != payment.getTotalPrice()) {
            payment.fail();
            updateOrderStatus(payment.getOrder(), OrderStatus.PAYMENT_PENDING, OrderStatus.FAILED, "웹훅 금액 불일치");
            paymentLogRepository.save(new PaymentLog(
                    payment, PaymentStatus.PENDING, PaymentStatus.FAIL,
                    PaymentType.PORTONE, portonePaymentId,
                    portoneResult.getPaidAmount(), "웹훅 금액 불일치"
            ));
            log.error("[웹훅 금액 불일치] expected={}, actual={}", payment.getTotalPrice(), portoneResult.getPaidAmount());
            return;
        }

        payment.confirm(portonePaymentId);
        updateOrderStatus(payment.getOrder(), OrderStatus.PAYMENT_PENDING, OrderStatus.PAID, null);
        paymentLogRepository.save(new PaymentLog(
                payment, PaymentStatus.PENDING, PaymentStatus.SUCCESS,
                PaymentType.PORTONE, portonePaymentId,
                portoneResult.getPaidAmount(), null
        ));

        // Redis 인기 메뉴 점수 증가
        Order order = payment.getOrder();
        updatePopularMenu(order.getMenu().getId());

        eventPublisher.publishEvent(new OrderCompletedEvent(
                order.getUser().getUserKey(), order.getMenu().getId(), payment.getTotalPrice()
        ));

        log.info("[웹훅 결제 처리 완료] portonePaymentId={}, merchantUid={}", portonePaymentId, merchantUid);
    }

    private void updateOrderStatus(Order order, OrderStatus from, OrderStatus to, String reason) {
        order.updateStatus(to);
        orderLogRepository.save(new OrderLog(order, from, to, reason));
    }

    // Redis 장애가 결제 트랜잭션에 영향 주지 않도록 try-catch 처리
    private void updatePopularMenu(Long menuId) {
        try {
            String todayKey = POPULAR_KEY_PREFIX + LocalDate.now();
            redisTemplate.opsForZSet().incrementScore(todayKey, String.valueOf(menuId), 1);
            redisTemplate.expire(todayKey, Duration.ofDays(8));
        } catch (Exception e) {
            log.warn("Redis 인기 메뉴 업데이트 실패 - menuId: {}", menuId, e);
        }
    }
}