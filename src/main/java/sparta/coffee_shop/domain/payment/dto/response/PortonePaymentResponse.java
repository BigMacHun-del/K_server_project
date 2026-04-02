package sparta.coffee_shop.domain.payment.dto.response;

import lombok.Getter;
import sparta.coffee_shop.domain.payment.entity.Payment;
import sparta.coffee_shop.domain.payment.entity.PaymentStatus;

@Getter
public class PortonePaymentResponse {

    private final Long paymentId;
    private final String merchantUid;
    private final String portonePaymentId;
    private final PaymentStatus status;
    private final int totalPrice;

    private PortonePaymentResponse(Long paymentId, String merchantUid,
                                   String portonePaymentId, PaymentStatus status, int totalPrice) {
        this.paymentId = paymentId;
        this.merchantUid = merchantUid;
        this.portonePaymentId = portonePaymentId;
        this.status = status;
        this.totalPrice = totalPrice;
    }

    public static PortonePaymentResponse from(Payment payment) {
        return new PortonePaymentResponse(
                payment.getId(),
                payment.getMerchantUid(),
                payment.getPortonePaymentId(),
                payment.getStatus(),
                payment.getTotalPrice()
        );
    }
}