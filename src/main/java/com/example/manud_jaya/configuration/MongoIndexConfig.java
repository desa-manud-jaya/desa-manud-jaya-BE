package com.example.manud_jaya.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mapping.context.MappingContext;

@Configuration
@EnableMongoRepositories(basePackages = "com.example.manud_jaya.repository")
public class MongoIndexConfig {

    @Bean
    public MongoPersistentEntityIndexResolver mongoIndexResolver(
            MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext) {
        return new MongoPersistentEntityIndexResolver(mappingContext);
    }
}
