package org.example.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@EnableConfigurationProperties(DealServiceProperties.class)
@TestPropertySource(properties = {
        "springdoc.swagger-ui.path=/swagger-ui.html",
        "springdoc.swagger-ui.enabled=true",
        "springdoc.api-docs.enabled=true",
        "server.port=8888",
        "deal.service.url-statement=http://localhost:8081/deal/statement",
        "deal.service.url-select=http://localhost:8081/deal/offer/select"
})
public class DealServicePropertiesTest {

    @Autowired
    private DealServiceProperties dealServiceProperties;

    @Test
    void contextLoads() {
        assertNotNull(dealServiceProperties);
    }

    @Test
    void shouldLoadStatementUrlCorrectly() {
        assertEquals("http://localhost:8081/deal/statement",
                dealServiceProperties.getUrlStatement());
    }

    @Test
    void shouldLoadSelectUrlCorrectly() {
        assertEquals("http://localhost:8081/deal/offer/select",
                dealServiceProperties.getUrlSelect());
    }
}