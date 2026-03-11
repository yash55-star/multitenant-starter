package com.example.common.config.tenant;

import com.example.common.constant.GlobalConstants;
import com.example.common.model.BaseResponse;
import com.example.common.validator.RouteValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Component
@Order(1)
public class TenantFilter implements Filter {

    private final ObjectMapper objectMapper;
    private final RouteValidator routeValidator;

    @Value("${allowed.tenants.request}")
    private List<String> allowedTenants;

    public TenantFilter(ObjectMapper objectMapper, RouteValidator routeValidator) {
        this.objectMapper = objectMapper;
        this.routeValidator = routeValidator;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestUri = httpRequest.getRequestURI();
        String tenantName = getTenantId(httpRequest);

        if (!routeValidator.isUrlSecured.test(requestUri)) {
            if (!StringUtils.hasText(tenantName)) {
                sendErrorResponse((HttpServletResponse) response, "Tenant ID is required");
                return;
            }
            if (!allowedTenants.contains(tenantName)) {
                sendErrorResponse((HttpServletResponse) response, "Tenant ID is invalid or not allowed");
                return;
            }
        }

        if (StringUtils.hasText(tenantName)) {
            TenantContext.setCurrentTenant(tenantName);
        }

        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    public String getTenantId(HttpServletRequest request) {
        String identifierId = request.getHeader(GlobalConstants.IDENTIFIER);
        String tenantId = Arrays.stream(request.getCookies() != null ? request.getCookies() : new Cookie[0])
                .filter(cookie -> {
                    if (StringUtils.hasText(identifierId)) {
                        return (GlobalConstants.X_TENANT_ID + "_" + identifierId).equals(cookie.getName());
                    }
                    return GlobalConstants.X_TENANT_ID.equals(cookie.getName());
                })
                .map(Cookie::getValue)
                .findFirst()
                .orElseGet(() -> request.getHeader(GlobalConstants.X_TENANT_ID));

        if (!StringUtils.hasText(tenantId)) {
            return null;
        }

        try {
            return new String(Base64.getDecoder().decode(tenantId), StandardCharsets.UTF_8).toLowerCase();
        } catch (IllegalArgumentException ex) {
            return tenantId.toLowerCase();
        }
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(
                new BaseResponse<>(message, true, HttpStatus.BAD_REQUEST)
        ));
    }
}
