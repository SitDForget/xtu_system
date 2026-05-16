package com.xtu.system.modules.system.user.vo;

import java.util.List;

public class UserDetailVO extends UserPageItemVO {

    private Integer gender;
    private List<Long> roleIds;

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }
}
