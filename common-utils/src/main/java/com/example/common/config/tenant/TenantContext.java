package com.example.common.config.tenant;

public final class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static String getCurrentTenant() {
        return CURRENT_TENANT.get();
    }

    public static void setCurrentTenant(String tenant) {
        CURRENT_TENANT.set(tenant == null ? null : tenant.toLowerCase());
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
