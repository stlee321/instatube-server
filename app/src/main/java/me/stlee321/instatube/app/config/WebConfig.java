package me.stlee321.instatube.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import me.stlee321.instatube.app.controller.converter.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {
    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new SignInFormConverter(objectMapper()));
        registry.addConverter(new PageRequestDirectionConverter());
        registry.addConverter(new PostFormConverter(objectMapper()));
        registry.addConverter(new SetMeFormConverter(objectMapper()));
        registry.addConverter(new NotificationConverter(objectMapper()));
        WebMvcConfigurer.super.addFormatters(registry);
    }

}
