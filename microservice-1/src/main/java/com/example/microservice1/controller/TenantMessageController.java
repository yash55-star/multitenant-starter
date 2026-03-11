package com.example.microservice1.controller;

import com.example.common.constant.GlobalConstants;
import com.example.common.model.BaseResponse;
import com.example.microservice1.entity.TenantMessage;
import com.example.microservice1.service.TenantMessageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(GlobalConstants.BASE_REST_URL + "/messages")
public class TenantMessageController {

    private final TenantMessageService tenantMessageService;

    public TenantMessageController(TenantMessageService tenantMessageService) {
        this.tenantMessageService = tenantMessageService;
    }

    @GetMapping("/current")
    public BaseResponse<TenantMessageService.TenantSummary> currentTenantSummary() {
        return new BaseResponse<>(tenantMessageService.currentSummary());
    }

    @GetMapping
    public BaseResponse<List<TenantMessage>> getMessages() {
        return new BaseResponse<>(tenantMessageService.getAllMessages());
    }

    @PostMapping
    public BaseResponse<TenantMessage> createMessage(@RequestBody Map<String, String> request) {
        return new BaseResponse<>(tenantMessageService.createMessage(request.getOrDefault("message", "default message")));
    }
}
