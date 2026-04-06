package sparta.coffee_shop.common.portone;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class PortoneClient {

    private final WebClient portoneWebClient;

    public PortonePaymentResult getPayment(String portonePaymentId) {
        return portoneWebClient.get()
                .uri("/payments/{paymentId}", portonePaymentId)
                .retrieve()
                .bodyToMono(PortonePaymentResult.class)
                .doOnError(e -> log.error("[포트원 조회 실패] paymentId={}", portonePaymentId, e))
                .block();
    }
}