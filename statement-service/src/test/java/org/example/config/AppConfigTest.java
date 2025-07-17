package org.example.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AppConfigTest {

    @Test
    void testRestTemplateBeanCreation() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        RestTemplate restTemplate = context.getBean(RestTemplate.class);
        assertNotNull(restTemplate, "RestTemplate bean should be created");
        assertEquals(RestTemplate.class, restTemplate.getClass());

        context.close();
    }
}