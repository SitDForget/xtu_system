package com.xtu.system.modules.system.user.mapper;

import com.xtu.system.modules.system.user.dto.UserQueryRequest;
import com.xtu.system.modules.system.user.entity.UserEntity;
import com.xtu.system.modules.system.user.vo.UserDetailVO;
import com.xtu.system.modules.system.user.vo.UserOptionVO;
import com.xtu.system.modules.system.user.vo.UserPageItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    List<UserPageItemVO> selectUserPage(UserQueryRequest request);

    long countUserPage(UserQueryRequest request);

    UserDetailVO selectUserDetailById(@Param("id") Long id);

    UserEntity selectUserEntityById(@Param("id") Long id);

    UserEntity selectUserEntityByUsername(@Param("username") String username);

    int insertUser(UserEntity entity);

    int updateUser(UserEntity entity);

    int updateUserStatus(@Param("id") Long id, @Param("status") Integer status, @Param("updatedBy") Long updatedBy);

    int updatePasswordById(@Param("id") Long id, @Param("passwordHash") String passwordHash, @Param("updatedBy") Long updatedBy);

    int logicDeleteUser(@Param("id") Long id, @Param("updatedBy") Long updatedBy);

    int updateLastLoginAt(@Param("id") Long id);

    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);

    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    List<UserOptionVO> selectUserOptions(UserQueryRequest request);

    int deleteUserRolesByUserId(@Param("userId") Long userId);

    int insertUserRoles(@Param("userId") Long userId, @Param("operatorId") Long operatorId, @Param("roleIds") List<Long> roleIds);
}
