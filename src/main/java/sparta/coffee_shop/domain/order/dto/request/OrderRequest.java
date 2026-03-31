package sparta.coffee_shop.domain.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class OrderRequest {

    @NotBlank(message = "사용자 식별값은 필수입니다.")
    private String userKey;

    @NotNull(message = "메뉴 ID는 필수입니다.")
    private Long menuId;
}