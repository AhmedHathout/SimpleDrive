//package com.ahmedhathout.SimpleDrive.configurations;
//
//import com.mongodb.reactivestreams.client.MongoClient;
//import com.mongodb.reactivestreams.client.MongoClients;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Profile;
//import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
//
//@Configuration
//@Profile("prod")
//public class ProdMongoConfigurations extends AbstractReactiveMongoConfiguration {
//
//    @Override
//    @Bean
//    public MongoClient reactiveMongoClient() {
//        // TODO Add credentials if possible
//        return MongoClients.create();
//    }
//
//    @Override
//    protected String getDatabaseName() {
//        return "simple_drive_db";
//    }
//}
