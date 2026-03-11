package com.example.common.config.tenant;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

@Configuration
public class MultiTenantMongoDbFactory extends SimpleMongoClientDatabaseFactory {

    private final TenantConfiguration tenantConfiguration;

    public MultiTenantMongoDbFactory(@Qualifier("defaultMongoClient") MongoClient mongoClient,
                                     @Qualifier("defaultMongoDatabase") String databaseName,
                                     TenantConfiguration tenantConfiguration) {
        super(mongoClient, databaseName);
        this.tenantConfiguration = tenantConfiguration;
    }

    @Override
    public MongoDatabase getMongoDatabase() throws DataAccessException {
        return tenantConfiguration.getMongoTemplate().getDb();
    }
}
