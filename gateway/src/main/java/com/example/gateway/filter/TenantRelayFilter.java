package com.example.gateway.filter;

import com.example.gateway.constant.GatewayConstants;
import com.example.gateway.validator.RouteValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Component
public class TenantRelayFilter implements GatewayFilter {

    private final RouteValidator routeValidator;

    @Value("${allowed.tenants.request}")
    private List<String> allowedTenants;

    public TenantRelayFilter(RouteValidator routeValidator) {
        this.routeValidator = routeValidator;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (routeValidator.isUrlSecured.test(path)) {
            return chain.filter(exchange);
        }

        String encodedTenant = getTenantId(exchange.getRequest());
        if (!StringUtils.hasText(encodedTenant)) {
            exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
            return exchange.getResponse().setComplete();
        }

        String tenant = decodeTenant(encodedTenant);
        if (!allowedTenants.contains(tenant)) {
            exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
            return exchange.getResponse().setComplete();
        }

        ServerHttpRequest request = exchange.getRequest().mutate()
                .header(GatewayConstants.X_TENANT_ID, Base64.getEncoder().encodeToString(tenant.getBytes(StandardCharsets.UTF_8)))
                .build();
        return chain.filter(exchange.mutate().request(request).build());
    }

    private String getTenantId(ServerHttpRequest request) {
        HttpCookie tenantCookie = request.getCookies().getFirst(GatewayConstants.X_TENANT_ID);
        if (tenantCookie != null) {
            return tenantCookie.getValue();
        }
        List<String> headerValues = request.getHeaders().get(GatewayConstants.X_TENANT_ID);
        if (headerValues != null && !headerValues.isEmpty()) {
            return headerValues.getFirst();
        }
        return null;
    }

    private String decodeTenant(String value) {
        try {
            return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8).toLowerCase();
        } catch (IllegalArgumentException ex) {
            return value.toLowerCase();
        }
    }
}
