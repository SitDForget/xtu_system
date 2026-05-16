package com.xtu.system.modules.system.user.controller;

import com.xtu.system.common.api.ApiResponse;
import com.xtu.system.common.api.PageResponse;
import com.xtu.system.modules.system.user.dto.UserCreateRequest;
import com.xtu.system.modules.system.user.dto.UserQueryRequest;
import com.xtu.system.modules.system.user.dto.UserResetPasswordRequest;
import com.xtu.system.modules.system.user.dto.UserRoleAssignRequest;
import com.xtu.system.modules.system.user.dto.UserStatusUpdateRequest;
import com.xtu.system.modules.system.user.dto.UserUpdateRequest;
import com.xtu.system.modules.system.user.service.UserService;
import com.xtu.system.modules.system.user.vo.UserDetailVO;
import com.xtu.system.modules.system.user.vo.UserOptionVO;
import com.xtu.system.modules.system.user.vo.UserPageItemVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('system:user:view')")
    public ApiResponse<PageResponse<UserPageItemVO>> getUserPage(UserQueryRequest request) {
        return ApiResponse.success(userService.getUserPage(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:view')")
    public ApiResponse<UserDetailVO> getUserDetail(@PathVariable Long id) {
        return ApiResponse.success(userService.getUserDetail(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system:user:create')")
    public ApiResponse<Long> createUser(@Valid @RequestBody UserCreateRequest request) {
        return ApiResponse.success("创建成功", userService.createUser(request));
    }

    @GetMapping("/options")
    @PreAuthorize("hasAuthority('system:user:view')")
    public ApiResponse<List<UserOptionVO>> getUserOptions(UserQueryRequest request) {
        return ApiResponse.success(userService.getUserOptions(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:update')")
    public ApiResponse<Boolean> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        userService.updateUser(id, request);
        return ApiResponse.success("更新成功", Boolean.TRUE);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('system:user:update')")
    public ApiResponse<Boolean> updateUserStatus(@PathVariable Long id, @Valid @RequestBody UserStatusUpdateRequest request) {
        userService.updateUserStatus(id, request.getStatus());
        return ApiResponse.success("状态更新成功", Boolean.TRUE);
    }

    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('system:user:update')")
    public ApiResponse<Boolean> resetUserPassword(@PathVariable Long id, @Valid @RequestBody UserResetPasswordRequest request) {
        userService.resetUserPassword(id, request.getPassword());
        return ApiResponse.success("密码重置成功", Boolean.TRUE);
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('system:user:update')")
    public ApiResponse<Boolean> assignUserRoles(@PathVariable Long id, @Valid @RequestBody UserRoleAssignRequest request) {
        userService.assignUserRoles(id, request.getRoleIds());
        return ApiResponse.success("角色分配成功", Boolean.TRUE);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:delete')")
    public ApiResponse<Boolean> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success("删除成功", Boolean.TRUE);
    }
}
