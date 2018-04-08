package com.demo.thymeleaf;

import com.demo.thymeleaf.dialect.QueryStringDialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringThymeleafConfig {

    @Bean
    public QueryStringDialect queryStringDialect() {
        System.out.println("SPRING BOOT ADDING - QueryStringDialect");
        return new QueryStringDialect();
    }
}
