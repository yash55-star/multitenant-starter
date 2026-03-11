package com.example.gateway.controller;

import com.example.gateway.dto.AuthRequest;
import com.example.gateway.service.AuthGatewayService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

import java.util.Map;

@Controller
public class UiController {

    @GetMapping("/")
    public Rendering home() {
        return Rendering.view("index")
                .modelAttribute("defaultTenant", "tenant_alpha")
                .modelAttribute("defaultUsername", "test-user")
                .build();
    }

    @RestController
    static class UiApiController {

        private final AuthGatewayService authGatewayService;

        UiApiController(AuthGatewayService authGatewayService) {
            this.authGatewayService = authGatewayService;
        }

        @PostMapping(value = "/ui/api/register", produces = MediaType.APPLICATION_JSON_VALUE)
        public Mono<Map<String, Object>> register(@RequestBody AuthRequest request) {
            return authGatewayService.register(request);
        }

        @PostMapping(value = "/ui/api/token", produces = MediaType.APPLICATION_JSON_VALUE)
        public Mono<Map<String, Object>> login(@RequestBody AuthRequest request) {
            return authGatewayService.login(request);
        }
    }
}
