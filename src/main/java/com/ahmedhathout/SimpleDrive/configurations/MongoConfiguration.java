package com.ahmedhathout.SimpleDrive.configurations;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

@Configuration
@Log4j2
public class MongoConfiguration extends AbstractMongoClientConfiguration {

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Value("${MongoDBUri}")
    private String mongoDBUri;

    public String testDBName = "simple_drive_test_db";
    public String developmentDBName = "simple_drive_development_db";
    public String productionDBName = "simple_drive_db";

    // This could have been done with @Profile instead but there would have been duplicated code.
    @Override
    protected String getDatabaseName() {
        switch (activeProfile) {
            case "test": return testDBName;
            case "dev": return developmentDBName;
            case "prod": return productionDBName;
            default: log.error("Unknown DB Name: " + activeProfile); return testDBName;
        }
    }

    @Override
    public MongoClient mongoClient() {
        return MongoClients.create(mongoDBUri);
    }
}
