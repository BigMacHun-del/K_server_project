package sparta.coffee_shop.domain.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PortonePaymentRequest {

    @NotBlank(message = "merchantUid는 필수입니다.")
    private String merchantUid;

    @NotBlank(message = "portonePaymentId는 필수입니다.")
    private String portonePaymentId;
}