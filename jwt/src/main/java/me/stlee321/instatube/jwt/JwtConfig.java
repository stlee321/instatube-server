package me.stlee321.instatube.jwt;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:jwt-application.properties")
@ComponentScan(basePackages = "me.stlee321.instatube.jwt")
public class JwtConfig {
    @Bean
    @ConditionalOnMissingBean
    SubjectValidator subjectValidator() {
        return new PassSubjectValidator();
    }
}
