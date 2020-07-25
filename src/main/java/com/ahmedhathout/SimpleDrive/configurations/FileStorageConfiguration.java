package com.ahmedhathout.SimpleDrive.configurations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

@Configuration
public class FileStorageConfiguration {

    @Autowired
    private AbstractMongoClientConfiguration abstractMongoClientConfiguration;

    @Bean
    public GridFsTemplate gridFsTemplate() throws Exception {
        return new GridFsTemplate(abstractMongoClientConfiguration.mongoDbFactory(),
                abstractMongoClientConfiguration.mappingMongoConverter());
    }
}
