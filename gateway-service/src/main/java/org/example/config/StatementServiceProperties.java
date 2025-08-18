package org.example.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
public class StatementServiceProperties {
    @Value("${statement.service.url-service}")
    private String urlService;

    @Value("${statement.service.endpoint.statement}")
    private String statementEndpoint;

    @Value("${statement.service.endpoint.offer}")
    private String offerEndpoint;

    public String getFullStatementUrl() {
        return urlService + statementEndpoint;
    }

    public String getFullOfferUrl() {
        return urlService + offerEndpoint;
    }
}
