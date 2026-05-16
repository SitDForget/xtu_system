package com.xtu.system.modules.auth.controller;

import com.xtu.system.common.api.ApiResponse;
import com.xtu.system.modules.auth.dto.LoginRequest;
import com.xtu.system.modules.auth.dto.UpdatePasswordRequest;
import com.xtu.system.modules.auth.service.AuthService;
import com.xtu.system.modules.auth.vo.CurrentUserResponse;
import com.xtu.system.modules.auth.vo.LoginResponse;
import com.xtu.system.modules.system.menu.vo.MenuTreeVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("登录成功", authService.login(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Boolean> logout() {
        return ApiResponse.success("退出成功", Boolean.TRUE);
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserResponse> getCurrentUser(
        @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return ApiResponse.success(authService.getCurrentUser(authorizationHeader));
    }

    @GetMapping("/menus")
    public ApiResponse<List<MenuTreeVO>> getMenus(
        @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return ApiResponse.success(authService.getMenus(authorizationHeader));
    }

    @GetMapping("/permissions")
    public ApiResponse<List<String>> getPermissions(
        @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return ApiResponse.success(authService.getPermissions(authorizationHeader));
    }

    @PutMapping("/password")
    public ApiResponse<Boolean> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
        authService.updatePassword(request);
        return ApiResponse.success("密码修改成功", Boolean.TRUE);
    }
}
