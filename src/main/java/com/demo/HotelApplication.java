package com.demo;

import com.github.mjstewart.querystring.dialect.QueryStringDialect;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class HotelApplication {

	public static void main(String[] args) {
		SpringApplication.run(HotelApplication.class, args);
	}

	@Bean
	public TimeProvider timeProvider() {
		return new TimeProvider();
	}

	@Bean
	public QueryStringDialect queryStringDialect() {
		return new QueryStringDialect();
	}
}
