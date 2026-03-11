package com.example.authservice.controller;

import com.example.authservice.dto.AuthRequest;
import com.example.authservice.dto.AuthValidationResponse;
import com.example.authservice.service.AppUserService;
import com.example.common.constant.GlobalConstants;
import com.example.common.model.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(GlobalConstants.BASE_REST_URL + "/auth")
public class AuthController {
    

    private final AppUserService appUserService;

    public AuthController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @PostMapping("/register")
    public BaseResponse<AuthValidationResponse> register(@RequestBody AuthRequest request) {
        AuthValidationResponse response = appUserService.register(request);
        HttpStatus status = response.isValid() ? HttpStatus.CREATED : HttpStatus.CONFLICT;
        return new BaseResponse<>(response, response.getMessage(), !response.isValid(), status);
    }

    @PostMapping("/validate")
    public BaseResponse<AuthValidationResponse> validate(@RequestBody AuthRequest request) {
        AuthValidationResponse response = appUserService.validateLogin(request);
        HttpStatus status = response.isValid() ? HttpStatus.OK : HttpStatus.UNAUTHORIZED;
        return new BaseResponse<>(response, response.getMessage(), !response.isValid(), status);
    }
}
