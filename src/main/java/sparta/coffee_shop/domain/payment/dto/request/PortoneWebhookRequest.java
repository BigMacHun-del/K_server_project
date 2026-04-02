package sparta.coffee_shop.domain.payment.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PortoneWebhookRequest {

    private String type;       // "Transaction.Paid" 등
    private String timestamp;
    private Data data;

    @Getter
    @Setter
    public static class Data {
        private String paymentId;
        private String merchantId;
        private String storeId;
        private String transactionId;
    }
}