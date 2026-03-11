package com.example.common.config.tenant;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class TenantPropertyConfig {

    private final ConcurrentHashMap<String, TenantProperties> tenants = new ConcurrentHashMap<>();

    public void setTenantConfig(TenantProperties tenantConfig) {
        tenants.put(tenantConfig.getClientCode(), tenantConfig);
    }

    public TenantProperties getTenantConfig(String tenantName) {
        TenantProperties tenantConfig = tenants.get(tenantName);
        if (tenantConfig == null) {
            throw new IllegalArgumentException("Tenant config does not exist for key: " + tenantName);
        }
        return tenantConfig;
    }

    public String getProperty(String key) {
        TenantProperties tenantConfig = getTenantConfig(TenantContext.getCurrentTenant());
        Object value = tenantConfig.getProperties().get(key);
        return value == null ? null : value.toString();
    }
}
