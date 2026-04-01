package sparta.coffee_shop.domain.menu.dto.response;

import lombok.Getter;
import sparta.coffee_shop.domain.menu.entity.Menu;

@Getter
public class MenuResponse {

    private final Long menuId;
    private final String productName;
    private final int price;

    private MenuResponse(Long menuId, String productName, int price) {
        this.menuId = menuId;
        this.productName = productName;
        this.price = price;
    }

    public static MenuResponse from(Menu menu) {
        return new MenuResponse(menu.getId(), menu.getProductName(), menu.getPrice());
    }
}
