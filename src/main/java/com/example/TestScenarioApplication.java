package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class TestScenarioApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestScenarioApplication.class, args);
        System.out.println("\n=== Test Scenario Management System Started ===");
        System.out.println("Web Interface: http://localhost:8080");
        System.out.println("API Endpoints: http://localhost:8080/api/test-scenarios");
        System.out.println("===============================================\n");
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(false);
            }
        };
    }
}