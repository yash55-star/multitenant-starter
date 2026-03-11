package com.example.gateway.service;

import com.example.gateway.dto.AuthRequest;
import com.example.gateway.dto.AuthValidationResponse;
import com.example.gateway.dto.BaseResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Service
public class AuthGatewayService {

    private final WebClient webClient;
    private final JwtService jwtService;

    public AuthGatewayService(WebClient.Builder webClientBuilder,
                              JwtService jwtService,
                              @Value("${auth-service.base-url}") String authServiceBaseUrl) {
        this.webClient = webClientBuilder.baseUrl(authServiceBaseUrl).build();
        this.jwtService = jwtService;
    }

    public Mono<Map<String, Object>> register(AuthRequest request) {
        return postToAuthService("/auth-service/api/auth/register", request)
                .map(response -> Map.of(
                        "valid", response.getData() != null && response.getData().isValid(),
                        "message", response.getMessage(),
                        "username", response.getData() == null ? request.getUsername() : response.getData().getUsername()
                ));
    }

    public Mono<Map<String, Object>> login(AuthRequest request) {
        return postToAuthService("/auth-service/api/auth/validate", request)
                .map(response -> {
                    boolean valid = response.getData() != null && response.getData().isValid();
                    if (!valid) {
                        return Map.of(
                                "valid", false,
                                "message", response.getMessage(),
                                "username", request.getUsername()
                        );
                    }

                    String token = jwtService.generateToken(response.getData().getUsername());
                    return Map.of(
                            "valid", true,
                            "message", "JWT generated successfully",
                            "username", response.getData().getUsername(),
                            "token", token
                    );
                });
    }

    private Mono<BaseResponseDto<AuthValidationResponse>> postToAuthService(String path, AuthRequest request) {
        if (request == null || !StringUtils.hasText(request.getUsername())
                || !StringUtils.hasText(request.getPassword())
                || !StringUtils.hasText(request.getTenantId())) {
            throw new IllegalArgumentException("Username, password, and tenantId are required");
        }

        return webClient.post()
                .uri(path)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header("X-TenantID", Base64.getEncoder().encodeToString(request.getTenantId().getBytes(StandardCharsets.UTF_8)))
                .bodyValue(Map.of(
                        "username", request.getUsername(),
                        "password", request.getPassword()
                ))
                .exchangeToMono(clientResponse -> clientResponse.bodyToMono(
                        new ParameterizedTypeReference<BaseResponseDto<AuthValidationResponse>>() {
                        }))
                .switchIfEmpty(Mono.error(new IllegalStateException("No response from auth service")));
    }
}
