package sparta.coffee_shop.domain.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sparta.coffee_shop.common.exception.ErrorEnum;
import sparta.coffee_shop.common.exception.ServiceErrorException;
import sparta.coffee_shop.domain.menu.entity.Menu;
import sparta.coffee_shop.domain.menu.repository.MenuRepository;
import sparta.coffee_shop.domain.order.dto.request.PortoneOrderRequest;
import sparta.coffee_shop.domain.order.dto.response.PortoneOrderResponse;
import sparta.coffee_shop.domain.order.entity.Order;
import sparta.coffee_shop.domain.order.entity.OrderStatus;
import sparta.coffee_shop.domain.order.repository.OrderRepository;
import sparta.coffee_shop.domain.orderlog.entity.OrderLog;
import sparta.coffee_shop.domain.orderlog.repository.OrderLogRepository;
import sparta.coffee_shop.domain.payment.entity.Payment;
import sparta.coffee_shop.domain.payment.entity.PaymentStatus;
import sparta.coffee_shop.domain.payment.entity.PaymentType;
import sparta.coffee_shop.domain.payment.repository.PaymentRepository;
import sparta.coffee_shop.domain.paymentLog.entity.PaymentLog;
import sparta.coffee_shop.domain.paymentLog.repository.PaymentLogRepository;
import sparta.coffee_shop.domain.user.entity.User;
import sparta.coffee_shop.domain.user.repository.UserRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortoneOrderService {

    private final UserRepository userRepository;
    private final MenuRepository menuRepository;
    private final OrderRepository orderRepository;
    private final OrderLogRepository orderLogRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentLogRepository paymentLogRepository;

    @Transactional
    public PortoneOrderResponse createOrder(PortoneOrderRequest request) {
        // 1. 유저 조회
        User user = userRepository.findByUserKey(request.getUserKey())
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_USER_NOT_FOUND));

        // 2. 메뉴 조회
        Menu menu = menuRepository.findById(request.getMenuId())
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_MENU_NOT_FOUND));

        if (!menu.isActive()) {
            throw new ServiceErrorException(ErrorEnum.ERR_MENU_NOT_ACTIVE);
        }

        // 3. merchantUid 생성 (우리 시스템 주문 식별자 → 포트원으로 전달)
        String merchantUid = "order-" + UUID.randomUUID();

        // 4. 주문 생성 (PENDING → PAYMENT_PENDING)
        Order order = orderRepository.save(new Order(user, menu, menu.getPrice()));
        order.updateStatus(OrderStatus.PAYMENT_PENDING);
        orderLogRepository.save(new OrderLog(order, OrderStatus.PENDING, OrderStatus.PAYMENT_PENDING, null));

        // 5. Payment 미리 생성 (PENDING 상태 — 이후 웹훅/검증에서 업데이트)
        // 예정 결제 금액 미리 저장
        Payment savedPayment = paymentRepository.save(new Payment(order, menu.getPrice(), merchantUid, null));
        paymentLogRepository.save(new PaymentLog(
                savedPayment, null, PaymentStatus.PENDING,
                PaymentType.PORTONE, null, menu.getPrice(), "포트원 결제 시작"
        ));

        log.info("[포트원 주문 생성] orderId={}, merchantUid={}, amount={}", order.getId(), merchantUid, menu.getPrice());

        return PortoneOrderResponse.of(order, merchantUid);
    }
}