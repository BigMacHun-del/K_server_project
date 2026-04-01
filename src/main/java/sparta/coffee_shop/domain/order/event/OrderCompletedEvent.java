package sparta.coffee_shop.domain.order.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class OrderCompletedEvent {

    private final String userKey;
    private final Long menuId;
    private final int amount;
}