package org.example.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "deal.service")
@Getter
@Setter
public class DealServiceProperties {
    private String urlStatement;
    private String urlSelect;
}
