package sparta.coffee_shop.domain.menu.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import sparta.coffee_shop.domain.menu.entity.Menu;

import java.io.Serializable;

@Getter
@NoArgsConstructor
public class MenuResponse implements Serializable {

    private Long menuId;
    private String productName;
    private int price;

    private MenuResponse(Long menuId, String productName, int price) {
        this.menuId = menuId;
        this.productName = productName;
        this.price = price;
    }

    public static MenuResponse from(Menu menu) {
        return new MenuResponse(menu.getId(), menu.getProductName(), menu.getPrice());
    }
}