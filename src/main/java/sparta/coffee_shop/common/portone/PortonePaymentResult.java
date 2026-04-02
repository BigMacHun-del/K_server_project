package sparta.coffee_shop.common.portone;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PortonePaymentResult {

    private String id;
    private String status;
    private Amount amount;

    @JsonProperty("orderName")
    private String orderName;

    @JsonProperty("customData")
    private String customData;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Amount {
        private int total;
        private int paid;
    }

    public boolean isPaid() {
        return "PAID".equals(this.status);
    }

    public int getPaidAmount() {
        return amount != null ? amount.getPaid() : 0;
    }
}