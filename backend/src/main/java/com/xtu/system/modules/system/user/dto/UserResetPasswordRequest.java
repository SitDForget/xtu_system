package com.xtu.system.modules.system.user.dto;

import jakarta.validation.constraints.NotBlank;

public class UserResetPasswordRequest {

    @NotBlank(message = "新密码不能为空")
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
