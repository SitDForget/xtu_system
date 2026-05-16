package com.xtu.system.modules.system.user.service;

import com.xtu.system.common.api.PageResponse;
import com.xtu.system.modules.system.user.dto.UserCreateRequest;
import com.xtu.system.modules.system.user.dto.UserQueryRequest;
import com.xtu.system.modules.system.user.vo.UserOptionVO;
import com.xtu.system.modules.system.user.dto.UserUpdateRequest;
import com.xtu.system.modules.system.user.vo.UserDetailVO;
import com.xtu.system.modules.system.user.vo.UserPageItemVO;

import java.util.List;

public interface UserService {

    PageResponse<UserPageItemVO> getUserPage(UserQueryRequest request);

    UserDetailVO getUserDetail(Long id);

    Long createUser(UserCreateRequest request);

    void updateUser(Long id, UserUpdateRequest request);

    void updateUserStatus(Long id, Integer status);

    void resetUserPassword(Long id, String password);

    void assignUserRoles(Long id, List<Long> roleIds);

    List<UserOptionVO> getUserOptions(UserQueryRequest request);

    void deleteUser(Long id);
}
