package com.xtu.system.modules.auth.service;

import com.xtu.system.modules.auth.dto.LoginRequest;
import com.xtu.system.modules.auth.dto.UpdatePasswordRequest;
import com.xtu.system.modules.auth.vo.CurrentUserResponse;
import com.xtu.system.modules.auth.vo.LoginResponse;
import com.xtu.system.modules.system.menu.vo.MenuTreeVO;

import java.util.List;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    CurrentUserResponse getCurrentUser(String authorizationHeader);

    List<MenuTreeVO> getMenus(String authorizationHeader);

    List<String> getPermissions(String authorizationHeader);

    void updatePassword(UpdatePasswordRequest request);
}
