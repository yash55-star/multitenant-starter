package com.example.common.validator;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {

    public static final List<String> UNPROTECTED_URLS = List.of(
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**",
            "/actuator/**"
    );

    public final Predicate<String> isUrlSecured = this::isUnprotected;

    protected boolean isUnprotected(String requestUri) {
        String[] sanitized = UNPROTECTED_URLS.stream()
                .map(url -> url.endsWith("/**") ? url.substring(0, url.length() - 3) : url)
                .toArray(String[]::new);
        return Arrays.stream(sanitized).anyMatch(requestUri::startsWith)
                || Arrays.stream(sanitized).anyMatch(requestUri::endsWith)
                || Arrays.stream(sanitized).anyMatch(requestUri::contains);
    }
}
