package com.example.common.config.tenant;

import com.mongodb.client.MongoClient;

public class MongoDbTenantDataSource {

    private String tenantId;
    private String host;
    private int port;
    private String database;
    private String username;
    private String password;
    private MongoClient client;

    public static Builder builder() {
        return new Builder();
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public MongoClient getClient() {
        return client;
    }

    public void setClient(MongoClient client) {
        this.client = client;
    }

    public static final class Builder {
        private String tenantId;
        private String host;
        private int port;
        private String database;
        private String username;
        private String password;
        private MongoClient client;

        private Builder() {
        }

        public Builder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder database(String database) {
            this.database = database;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder client(MongoClient client) {
            this.client = client;
            return this;
        }

        public MongoDbTenantDataSource build() {
            MongoDbTenantDataSource dataSource = new MongoDbTenantDataSource();
            dataSource.setTenantId(tenantId);
            dataSource.setHost(host);
            dataSource.setPort(port);
            dataSource.setDatabase(database);
            dataSource.setUsername(username);
            dataSource.setPassword(password);
            dataSource.setClient(client);
            return dataSource;
        }
    }
}
