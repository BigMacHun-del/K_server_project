package sparta.coffee_shop.domain.order.dto.response;

import lombok.Getter;
import sparta.coffee_shop.domain.order.entity.Order;

@Getter
public class PortoneOrderResponse {

    private final Long orderId;
    private final String merchantUid;
    private final String productName;
    private final int amount;

    private PortoneOrderResponse(Long orderId, String merchantUid, String productName, int amount) {
        this.orderId = orderId;
        this.merchantUid = merchantUid;
        this.productName = productName;
        this.amount = amount;
    }

    public static PortoneOrderResponse of(Order order, String merchantUid) {
        return new PortoneOrderResponse(
                order.getId(),
                merchantUid,
                order.getMenu().getProductName(),
                order.getAmount()
        );
    }
}