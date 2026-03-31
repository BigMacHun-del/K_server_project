package sparta.coffee_shop.domain.point.dto.response;

import lombok.Getter;

@Getter
public class PointChargeResponse {

    private final String userKey;
    private final long balance;

    private PointChargeResponse(String userKey, long balance) {
        this.userKey = userKey;
        this.balance = balance;
    }

    public static PointChargeResponse of(String userKey, long balance) {
        return new PointChargeResponse(userKey, balance);
    }
}