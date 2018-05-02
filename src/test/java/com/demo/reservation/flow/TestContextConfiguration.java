package com.demo.reservation.flow;

import com.demo.util.TemplateUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestContextConfiguration {

    @Bean
    public TemplateUtil templateUtil() {
        return new TemplateUtil();
    }
}
