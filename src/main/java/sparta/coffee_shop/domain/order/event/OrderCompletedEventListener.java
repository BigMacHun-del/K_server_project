package sparta.coffee_shop.domain.order.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderCompletedEventListener {

    @Async
    @EventListener
    public void handleOrderCompleted(OrderCompletedEvent event) {
        try {
            // TODO: 실제 데이터 수집 플랫폼 API 호출로 교체
            log.info("[데이터 플랫폼 전송] userKey={}, menuId={}, amount={}",
                    event.getUserKey(), event.getMenuId(), event.getAmount());
        } catch (Exception e) {
            log.error("[데이터 플랫폼 전송 실패] userKey={}, menuId={}",
                    event.getUserKey(), event.getMenuId(), e);
        }
    }
}