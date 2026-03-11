package com.example.common.config.tenant;

import java.util.Map;

public class TenantProperties {

    private String clientCode;
    private Map<String, Object> properties;

    public static Builder builder() {
        return new Builder();
    }

    public String getClientCode() {
        return clientCode;
    }

    public void setClientCode(String clientCode) {
        this.clientCode = clientCode;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public static final class Builder {
        private String clientCode;
        private Map<String, Object> properties;

        private Builder() {
        }

        public Builder clientCode(String clientCode) {
            this.clientCode = clientCode;
            return this;
        }

        public Builder properties(Map<String, Object> properties) {
            this.properties = properties;
            return this;
        }

        public TenantProperties build() {
            TenantProperties tenantProperties = new TenantProperties();
            tenantProperties.setClientCode(clientCode);
            tenantProperties.setProperties(properties);
            return tenantProperties;
        }
    }
}
