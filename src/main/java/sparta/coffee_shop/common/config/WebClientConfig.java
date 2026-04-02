package sparta.coffee_shop.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final PortoneProperties portoneProperties;

    @Bean
    public WebClient portoneWebClient() {
        return WebClient.builder()
                .baseUrl(portoneProperties.getBaseUrl())
                .defaultHeader("Authorization", "PortOne " + portoneProperties.getSecret())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}