package org.example.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
public class DealServiceProperties {
    @Value("${deal.service.url-deal}")
    private String urlDeal;

    @Value("${deal.service.endpoint.registration}")
    private String registrationEndpoint;

    @Value("${deal.service.endpoint.document}")
    private String documentEndpoint;

    public String getFullRegistrationUrl() {
        return urlDeal + registrationEndpoint;
    }

    public String getFullDocumentUrl() {
        return urlDeal + documentEndpoint;
    }
}
