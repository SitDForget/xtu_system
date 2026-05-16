package com.xtu.system.modules.system.log.controller;

import com.xtu.system.common.api.ApiResponse;
import com.xtu.system.common.api.PageResponse;
import com.xtu.system.modules.system.log.dto.LoginLogQueryRequest;
import com.xtu.system.modules.system.log.service.LogService;
import com.xtu.system.modules.system.log.vo.LoginLogPageItemVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/login-logs")
public class LoginLogController {

    private final LogService logService;

    public LoginLogController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('system:log:login:view')")
    public ApiResponse<PageResponse<LoginLogPageItemVO>> getLoginLogPage(LoginLogQueryRequest request) {
        return ApiResponse.success(logService.getLoginLogPage(request));
    }
}
