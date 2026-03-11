package com.example.gateway.config;

import com.example.gateway.filter.TenantRelayFilter;
import com.example.gateway.filter.JwtAuthenticationFilter;
import io.netty.resolver.DefaultAddressResolverGroup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.config.HttpClientCustomizer;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    private final TenantRelayFilter tenantRelayFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${microservice-1.service.base-url}")
    private String microserviceOneBaseUrl;

    public GatewayConfig(TenantRelayFilter tenantRelayFilter, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.tenantRelayFilter = tenantRelayFilter;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(route -> route.path("/microservice-1/**")
                        .filters(filter -> filter.filter(jwtAuthenticationFilter)
                                .filter(tenantRelayFilter)
                                .rewritePath("/microservice-1/(?<segment>.*)", "/microservice-1/${segment}"))
                        .uri(microserviceOneBaseUrl))
                .build();
    }

    @Bean
    public HttpClientCustomizer httpClientCustomizer() {
        return httpClient -> httpClient.resolver(DefaultAddressResolverGroup.INSTANCE);
    }
}
