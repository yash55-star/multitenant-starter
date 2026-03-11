package com.example.common.config.tenant;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class TenantDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return TenantContext.getCurrentTenant();
    }
}
