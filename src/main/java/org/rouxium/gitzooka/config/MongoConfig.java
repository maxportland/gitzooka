package org.rouxium.gitzooka.config;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;

@Configuration
public class MongoConfig extends AbstractMongoConfiguration {

    @Override
    protected String getDatabaseName() {
        return "gitzooka";
    }

    @Override
    @Bean
    public Mongo mongo() throws Exception {
        return new MongoClient("localhost",27017);
    }

}
