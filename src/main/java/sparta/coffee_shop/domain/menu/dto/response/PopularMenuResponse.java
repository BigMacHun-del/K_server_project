package sparta.coffee_shop.domain.menu.dto.response;

import lombok.Getter;

@Getter
public class PopularMenuResponse {

    private final int rank;
    private final Long menuId;
    private final String productName;
    private final int price;
    private final long orderCount;

    private PopularMenuResponse(int rank, Long menuId, String productName, int price, long orderCount) {
        this.rank = rank;
        this.menuId = menuId;
        this.productName = productName;
        this.price = price;
        this.orderCount = orderCount;
    }

    public static PopularMenuResponse of(int rank, Long menuId, String productName, int price, long orderCount) {
        return new PopularMenuResponse(rank, menuId, productName, price, orderCount);
    }
}