package sparta.coffee_shop.domain.paymentLog.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sparta.coffee_shop.domain.payment.entity.Payment;
import sparta.coffee_shop.domain.payment.entity.PaymentStatus;
import sparta.coffee_shop.domain.payment.entity.PaymentType;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "payments_log")
public class PaymentLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 10)
    private PaymentStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 10)
    private PaymentStatus toStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false, length = 10)
    private PaymentType paymentType;

    @Column(name = "portone_payment_id", length = 100)
    private String portonePaymentId;

    // 결제 금액 (결제 완료 시점에 확정된 금액)
    @Column(nullable = false)
    private int amount;

    @Column(length = 255)
    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public PaymentLog(Payment payment, PaymentStatus fromStatus, PaymentStatus toStatus,
                      PaymentType paymentType, String portonePaymentId, int amount, String reason) {
        this.payment = payment;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.paymentType = paymentType;
        this.portonePaymentId = portonePaymentId;
        this.amount = amount;
        this.reason = reason;
    }
}