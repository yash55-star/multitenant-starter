package com.example.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static com.example.gateway.constant.GatewayConstants.ADD;
import static com.example.gateway.constant.GatewayConstants.IDENTIFIER;
import static com.example.gateway.constant.GatewayConstants.REMOVE;
import static com.example.gateway.constant.GatewayConstants.TOKEN;
import static com.example.gateway.constant.GatewayConstants.UNDERSCORE;
import static com.example.gateway.constant.GatewayConstants.X_TENANT_ID;

@Component
public class TokenFilter implements GatewayFilter {

    @Value("${instance.hostname:localhost}")
    private String hostname;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = exchange.getRequest().getQueryParams().getFirst("token");
        String action = exchange.getRequest().getQueryParams().getFirst("action");
        String uuid = exchange.getRequest().getQueryParams().getFirst("uuid");
        String tenantId = exchange.getRequest().getQueryParams().getFirst("tenantId");

        if (!StringUtils.hasText(action) || (!ADD.equals(action) && !REMOVE.equals(action))) {
            exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
            return exchange.getResponse().setComplete();
        }

        String suffix = StringUtils.hasText(uuid) ? UNDERSCORE + uuid : UNDERSCORE + UUID.randomUUID();
        if (REMOVE.equals(action)) {
            suffix = getIdentifierSuffix(exchange);
            if (!StringUtils.hasText(suffix)) {
                exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
                return exchange.getResponse().setComplete();
            }
        }

        boolean secure = !hostname.contains("localhost");
        String sameSite = secure ? "None" : "";
        String domain = secure ? hostname : "localhost";
        long maxAge = REMOVE.equals(action) ? 0 : 3600;

        ResponseCookie tokenCookie = ResponseCookie.from(TOKEN + suffix, token == null ? "" : token)
                .path("/")
                .domain(domain)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .maxAge(maxAge)
                .build();

        ResponseCookie tenantCookie = ResponseCookie.from(X_TENANT_ID + suffix, tenantId == null ? "" : tenantId)
                .path("/")
                .domain(domain)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .maxAge(maxAge)
                .build();

        exchange.getResponse().getHeaders().addAll("Set-Cookie", List.of(tokenCookie.toString(), tenantCookie.toString()));

        if (ADD.equals(action)) {
            return chain.filter(exchange)
                    .then(writeJson(exchange, suffix.replace(UNDERSCORE, "")));
        }
        return chain.filter(exchange).then(Mono.fromRunnable(() -> exchange.getResponse().setComplete()));
    }

    private String getIdentifierSuffix(ServerWebExchange exchange) {
        String identifier = exchange.getRequest().getHeaders().getFirst(IDENTIFIER);
        return StringUtils.hasText(identifier) ? UNDERSCORE + identifier : null;
    }

    private Mono<Void> writeJson(ServerWebExchange exchange, String value) {
        try {
            byte[] json = new ObjectMapper().writeValueAsBytes(value);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(json);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (Exception ex) {
            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            byte[] bytes = ex.getMessage().getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        }
    }
}
