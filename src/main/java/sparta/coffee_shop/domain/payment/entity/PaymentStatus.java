package sparta.coffee_shop.domain.payment.entity;

public enum PaymentStatus {
    PENDING,  // 결제 대기 (포트원 결제 시작)
    SUCCESS,
    FAIL
}
