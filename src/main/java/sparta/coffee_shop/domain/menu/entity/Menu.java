package sparta.coffee_shop.domain.menu.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sparta.coffee_shop.common.BaseEntity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "menus")
public class Menu extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(nullable = false)
    private int price;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    public Menu(String productName, int price) {
        this.productName = productName;
        this.price = price;
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }
}
