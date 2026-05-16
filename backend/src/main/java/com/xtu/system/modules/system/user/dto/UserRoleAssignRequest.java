package com.xtu.system.modules.system.user.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public class UserRoleAssignRequest {

    @NotNull(message = "角色集合不能为空")
    private List<Long> roleIds;

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }
}
