package sparta.coffee_shop.domain.order.entity;

public enum OrderStatus {
    PENDING,          // 주문 생성
    PAYMENT_PENDING,  // 포트원 결제 진행 중
    PAID,
    FAILED
}
