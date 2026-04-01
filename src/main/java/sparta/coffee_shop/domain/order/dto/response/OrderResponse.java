package sparta.coffee_shop.domain.order.dto.response;

import lombok.Getter;
import sparta.coffee_shop.domain.order.entity.Order;

import java.time.LocalDateTime;

@Getter
public class OrderResponse {

    private final Long orderId;
    private final Long menuId;
    private final String productName;
    private final int amount;
    private final long remainBalance;
    private final LocalDateTime orderedAt;

    private OrderResponse(Long orderId, Long menuId, String productName,
                          int amount, long remainBalance, LocalDateTime orderedAt) {
        this.orderId = orderId;
        this.menuId = menuId;
        this.productName = productName;
        this.amount = amount;
        this.remainBalance = remainBalance;
        this.orderedAt = orderedAt;
    }

    public static OrderResponse of(Order order, long remainBalance) {
        return new OrderResponse(
                order.getId(),
                order.getMenu().getId(),
                order.getMenu().getProductName(),
                order.getAmount(),
                remainBalance,
                order.getOrderedAt()
        );
    }
}