package sparta.coffee_shop.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "portone.api")
public class PortoneProperties {

    private String secret;
    private String baseUrl;
}