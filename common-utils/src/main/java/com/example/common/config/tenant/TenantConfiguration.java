package com.example.common.config.tenant;

import com.example.common.constant.GlobalConstants;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
public class TenantConfiguration {

    private final ResourcePatternResolver resourcePatternResolver;
    private final TenantPropertyConfig tenantPropertyConfig;
    private final Environment environment;
    private final Map<String, MongoDbTenantDataSource> mongoDataSources = new HashMap<>();
    private final Map<Object, Object> resolvedDataSources = new HashMap<>();

    @Value("${default-tenant}")
    private String defaultTenant;

    public TenantConfiguration(ResourcePatternResolver resourcePatternResolver,
                               TenantPropertyConfig tenantPropertyConfig,
                               Environment environment) {
        this.resourcePatternResolver = resourcePatternResolver;
        this.tenantPropertyConfig = tenantPropertyConfig;
        this.environment = environment;
    }

    @Bean(name = "dataSource")
    public DataSource multitenantDataSource() throws IOException {
        Resource[] resources = resourcePatternResolver.getResources("classpath*:tenants/*.properties");
        for (Resource resource : resources) {
            Properties properties = new Properties();
            try (InputStream inputStream = resource.getInputStream()) {
                properties.load(inputStream);
            }
            String tenantId = properties.getProperty(GlobalConstants.TENANT_NAME).toLowerCase();
            setUpTenantProperties(properties, tenantId);
            DataSource dataSource = DataSourceBuilder.create()
                    .driverClassName(resolve(properties.getProperty(GlobalConstants.DATASOURCE_DRIVER_CLASS_NAME)))
                    .url(resolve(properties.getProperty(GlobalConstants.DATASOURCE_URL)))
                    .username(resolve(properties.getProperty(GlobalConstants.DATASOURCE_USERNAME)))
                    .password(resolve(properties.getProperty(GlobalConstants.DATASOURCE_PASSWORD)))
                    .build();
            resolvedDataSources.put(tenantId, dataSource);
            loadMongoConnection(tenantId, properties);
        }

        AbstractRoutingDataSource dataSource = new TenantDataSource();
        dataSource.setTargetDataSources(resolvedDataSources);
        dataSource.setDefaultTargetDataSource(resolvedDataSources.get(defaultTenant));
        dataSource.afterPropertiesSet();
        return dataSource;
    }

    public void setUpTenantProperties(Properties properties, String tenantId) {
        Map<String, Object> values = new HashMap<>();
        properties.forEach((key, value) -> values.put(key.toString(), resolve(value.toString())));
        tenantPropertyConfig.setTenantConfig(TenantProperties.builder()
                .clientCode(tenantId)
                .properties(values)
                .build());
    }

    private void loadMongoConnection(String tenantId, Properties properties) {
        String database = resolve(properties.getProperty(GlobalConstants.MONGO_DB_DATABASE, "admin"));
        String host = resolve(properties.getProperty(GlobalConstants.MONGO_DB_HOST, "localhost"));
        int port = Integer.parseInt(resolve(properties.getProperty(GlobalConstants.MONGO_DB_PORT, "27017")));
        String username = resolve(properties.getProperty(GlobalConstants.MONGO_DB_USERNAME, ""));
        String password = resolve(properties.getProperty(GlobalConstants.MONGO_DB_PASSWORD, ""));

        ConnectionString connectionString;
        if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
            connectionString = new ConnectionString(
                    "mongodb://" + username + ":" + password + "@" + host + ":" + port + "/" + database + "?authSource=admin"
            );
        } else {
            connectionString = new ConnectionString("mongodb://" + host + ":" + port + "/" + database);
        }

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();

        mongoDataSources.put(tenantId, MongoDbTenantDataSource.builder()
                .tenantId(tenantId)
                .database(database)
                .host(host)
                .port(port)
                .username(username)
                .password(password)
                .client(MongoClients.create(settings))
                .build());
    }

    public MongoTemplate getMongoTemplate() {
        String tenantId = TenantContext.getCurrentTenant();
        if (!StringUtils.hasText(tenantId)) {
            tenantId = defaultTenant;
        }
        return getMongoTemplateByTenantId(tenantId);
    }

    public MongoTemplate getMongoTemplateByTenantId(String tenantId) {
        MongoDbTenantDataSource tenantDataSource = mongoDataSources.get(tenantId);
        if (tenantDataSource == null) {
            throw new IllegalArgumentException("Mongo tenant not configured: " + tenantId);
        }
        return new MongoTemplate(tenantDataSource.getClient(), tenantDataSource.getDatabase());
    }

    @Bean(name = "defaultMongoClient")
    public MongoClient defaultMongoClient() {
        MongoDbTenantDataSource tenantDataSource = mongoDataSources.get(defaultTenant);
        return tenantDataSource == null ? MongoClients.create() : tenantDataSource.getClient();
    }

    @Bean(name = "defaultMongoDatabase")
    public String defaultMongoDatabase() {
        MongoDbTenantDataSource tenantDataSource = mongoDataSources.get(defaultTenant);
        return tenantDataSource == null ? "admin" : tenantDataSource.getDatabase();
    }

    private String resolve(String value) {
        return environment.resolvePlaceholders(value);
    }
}
