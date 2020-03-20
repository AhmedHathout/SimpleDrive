package com.ahmedhathout.SimpleDrive.configurations;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;

@Configuration
public class MongoConfigurations extends AbstractReactiveMongoConfiguration {

    @Value("${spring.profiles.active}")
    private String activeProfile;

    public String developmentDBName = "simple_drive_development_db";
    public String productionDBName = "simple_drive_db";

    @Override
    @Bean
    public MongoClient reactiveMongoClient() {
        // TODO Add credentials if possible
        return MongoClients.create();
    }

    // This could have been done with @Profile instead but there would have been duplicated code.
    @Override
    protected String getDatabaseName() {
        return activeProfile.contains("dev")? developmentDBName : productionDBName;
    }
}
