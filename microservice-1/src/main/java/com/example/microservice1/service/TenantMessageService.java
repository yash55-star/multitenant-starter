package com.example.microservice1.service;

import com.example.common.config.tenant.TenantContext;
import com.example.common.config.tenant.TenantPropertyConfig;
import com.example.microservice1.entity.TenantMessage;
import com.example.microservice1.repository.TenantMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TenantMessageService {

    private final TenantMessageRepository tenantMessageRepository;
    private final TenantPropertyConfig tenantPropertyConfig;

    public TenantMessageService(TenantMessageRepository tenantMessageRepository,
                                TenantPropertyConfig tenantPropertyConfig) {
        this.tenantMessageRepository = tenantMessageRepository;
        this.tenantPropertyConfig = tenantPropertyConfig;
    }

    @Transactional(readOnly = true)
    public List<TenantMessage> getAllMessages() {
        ensureSeedData();
        return tenantMessageRepository.findAll();
    }

    @Transactional
    public TenantMessage createMessage(String message) {
        TenantMessage tenantMessage = new TenantMessage();
        tenantMessage.setMessage(message);
        return tenantMessageRepository.save(tenantMessage);
    }

    @Transactional(readOnly = true)
    public TenantSummary currentSummary() {
        ensureSeedData();
        return new TenantSummary(
                TenantContext.getCurrentTenant(),
                tenantPropertyConfig.getProperty("tenant.display.name"),
                tenantMessageRepository.count()
        );
    }

    @Transactional
    protected void ensureSeedData() {
        if (tenantMessageRepository.count() == 0) {
            TenantMessage tenantMessage = new TenantMessage();
            tenantMessage.setMessage("Welcome from " + TenantContext.getCurrentTenant());
            tenantMessageRepository.save(tenantMessage);
        }
    }

    public record TenantSummary(String tenantId, String displayName, long messageCount) {
    }
}
