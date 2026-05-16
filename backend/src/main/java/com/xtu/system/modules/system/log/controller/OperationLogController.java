package com.xtu.system.modules.system.log.controller;

import com.xtu.system.common.api.ApiResponse;
import com.xtu.system.common.api.PageResponse;
import com.xtu.system.modules.system.log.dto.OperationLogQueryRequest;
import com.xtu.system.modules.system.log.service.LogService;
import com.xtu.system.modules.system.log.vo.OperationLogPageItemVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/operation-logs")
public class OperationLogController {

    private final LogService logService;

    public OperationLogController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('system:log:operation:view')")
    public ApiResponse<PageResponse<OperationLogPageItemVO>> getOperationLogPage(OperationLogQueryRequest request) {
        return ApiResponse.success(logService.getOperationLogPage(request));
    }
}
