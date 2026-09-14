package com.example.cafequeue.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import reactor.core.publisher.Mono;

@Configuration
@EnableReactiveMongoRepositories(basePackages = "com.example.cafequeue")
@EnableReactiveMongoAuditing
public class MongoConfig {

    @Bean
    public org.springframework.data.domain.ReactiveAuditorAware<String> myAuditorProvider() {
        return () -> Mono.just("SYSTEM");
    }
}
