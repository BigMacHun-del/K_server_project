package sparta.coffee_shop.domain.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sparta.coffee_shop.common.exception.ErrorEnum;
import sparta.coffee_shop.common.exception.ServiceErrorException;
import sparta.coffee_shop.domain.menu.entity.Menu;
import sparta.coffee_shop.domain.menu.repository.MenuRepository;
import sparta.coffee_shop.domain.order.dto.request.OrderRequest;
import sparta.coffee_shop.domain.order.dto.response.OrderResponse;
import sparta.coffee_shop.domain.order.entity.Order;
import sparta.coffee_shop.domain.order.entity.OrderStatus;
import sparta.coffee_shop.domain.order.event.OrderCompletedEvent;
import sparta.coffee_shop.domain.order.repository.OrderRepository;
import sparta.coffee_shop.domain.orderlog.entity.OrderLog;
import sparta.coffee_shop.domain.orderlog.repository.OrderLogRepository;
import sparta.coffee_shop.domain.payment.entity.Payment;
import sparta.coffee_shop.domain.payment.entity.PaymentStatus;
import sparta.coffee_shop.domain.payment.repository.PaymentRepository;
import sparta.coffee_shop.domain.point.entity.Point;
import sparta.coffee_shop.domain.point.repository.PointRepository;
import sparta.coffee_shop.domain.pointlog.entity.PointLog;
import sparta.coffee_shop.domain.pointlog.entity.PointLogType;
import sparta.coffee_shop.domain.pointlog.repository.PointLogRepository;
import sparta.coffee_shop.domain.user.entity.User;
import sparta.coffee_shop.domain.user.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final UserRepository userRepository;
    private final MenuRepository menuRepository;
    private final PointRepository pointRepository;
    private final PointLogRepository pointLogRepository;
    private final OrderRepository orderRepository;
    private final OrderLogRepository orderLogRepository;
    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public OrderResponse order(OrderRequest request) {
        // 1. 유저 조회
        User user = userRepository.findByUserKey(request.getUserKey())
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_USER_NOT_FOUND));

        // 2. 메뉴 조회 및 활성 여부 확인
        Menu menu = menuRepository.findById(request.getMenuId())
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_MENU_NOT_FOUND));

        if (!menu.isActive()) {
            throw new ServiceErrorException(ErrorEnum.ERR_MENU_NOT_ACTIVE);
        }

        // 3. 포인트 조회
        Point point = pointRepository.findByUser(user)
                .orElseThrow(() -> new ServiceErrorException(ErrorEnum.ERR_POINT_NOT_FOUND));

        // 4. 주문 생성 (PENDING)
        Order order = orderRepository.save(new Order(user, menu, menu.getPrice()));
        orderLogRepository.save(new OrderLog(order, null, OrderStatus.PENDING, null));

        // 5. 포인트 차감 (잔액 부족 시 FAILED 처리)
        try {
            point.deduct(menu.getPrice());
        } catch (IllegalArgumentException e) {
            order.updateStatus(OrderStatus.FAILED);
            orderLogRepository.save(new OrderLog(order, OrderStatus.PENDING, OrderStatus.FAILED, e.getMessage()));
            paymentRepository.save(new Payment(order, menu.getPrice(), PaymentStatus.FAIL));
            throw new ServiceErrorException(ErrorEnum.ERR_POINT_INSUFFICIENT);
        }

        // 6. 포인트 차감 이력 저장
        pointLogRepository.save(new PointLog(user, point, PointLogType.USE, menu.getPrice(), point.getBalance()));

        // 7. 결제 생성 (SUCCESS)
        paymentRepository.save(new Payment(order, menu.getPrice(), PaymentStatus.SUCCESS));

        // 8. 주문 상태 PAID 변경
        order.updateStatus(OrderStatus.PAID);
        orderLogRepository.save(new OrderLog(order, OrderStatus.PENDING, OrderStatus.PAID, null));

        // 9. 데이터 수집 플랫폼 이벤트 발행 (@Async 비동기)
        eventPublisher.publishEvent(new OrderCompletedEvent(user.getUserKey(), menu.getId(), menu.getPrice()));

        return OrderResponse.of(order, point.getBalance());
    }
}