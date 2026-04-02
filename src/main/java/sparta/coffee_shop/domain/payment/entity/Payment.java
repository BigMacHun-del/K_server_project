package sparta.coffee_shop.domain.payment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sparta.coffee_shop.domain.order.entity.Order;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "payments",
        indexes = {
                @Index(name = "idx_payments_portone_payment_id", columnList = "portone_payment_id"),
                @Index(name = "idx_payments_merchant_uid", columnList = "merchant_uid")
        }
)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(name = "total_price", nullable = false)
    private int totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PaymentStatus status;

    // 포트원 결제 고유 ID (웹훅 멱등성 키)
    @Column(name = "portone_payment_id", length = 100)
    private String portonePaymentId;

    // 우리 시스템 주문 식별자 (포트원으로 전달하는 merchantUid)
    @Column(name = "merchant_uid", length = 100)
    private String merchantUid;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false, length = 10)
    private PaymentType paymentType;

    @Column(name = "paid_at", updatable = false)
    private LocalDateTime paidAt;

    @PrePersist
    protected void onCreate() {
        this.paidAt = LocalDateTime.now();
    }

    // 기존 포인트 결제용 생성자 (유지)
    public Payment(Order order, int totalPrice, PaymentStatus status) {
        this.order = order;
        this.totalPrice = totalPrice;
        this.status = status;
        this.paymentType = PaymentType.POINT;
        this.paidAt = LocalDateTime.now();
    }

    // 포트원 결제용 생성자
    public Payment(Order order, int totalPrice, String merchantUid, String portonePaymentId) {
        this.order = order;
        this.totalPrice = totalPrice;
        this.merchantUid = merchantUid;
        this.portonePaymentId = portonePaymentId;
        this.status = PaymentStatus.PENDING;
        this.paymentType = PaymentType.PORTONE;
        this.paidAt = LocalDateTime.now();
    }

    public void confirm(String portonePaymentId) {
        this.portonePaymentId = portonePaymentId;
        this.status = PaymentStatus.SUCCESS;
    }

    public void fail() {
        this.status = PaymentStatus.FAIL;
    }
}