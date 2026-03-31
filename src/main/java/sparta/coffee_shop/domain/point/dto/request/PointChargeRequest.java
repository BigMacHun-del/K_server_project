package sparta.coffee_shop.domain.point.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class PointChargeRequest {

    @NotBlank(message = "사용자 식별값은 필수입니다.")
    private String userKey;

    @Min(value = 1, message = "충전 금액은 1원 이상이어야 합니다.")
    private int amount;
}