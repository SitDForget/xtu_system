package com.xtu.system.modules.system.user.service.impl;

import com.xtu.system.common.api.PageResponse;
import com.xtu.system.common.exception.BusinessException;
import com.xtu.system.modules.personnel.student.mapper.StudentMapper;
import com.xtu.system.modules.personnel.teacher.mapper.TeacherMapper;
import com.xtu.system.modules.system.user.dto.UserCreateRequest;
import com.xtu.system.modules.system.user.dto.UserQueryRequest;
import com.xtu.system.modules.system.user.dto.UserUpdateRequest;
import com.xtu.system.modules.system.user.entity.UserEntity;
import com.xtu.system.modules.system.user.mapper.UserMapper;
import com.xtu.system.modules.system.user.service.UserService;
import com.xtu.system.modules.system.user.vo.UserDetailVO;
import com.xtu.system.modules.system.user.vo.UserOptionVO;
import com.xtu.system.modules.system.user.vo.UserPageItemVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private static final long SYSTEM_OPERATOR_ID = 1L;
    private static final long DEFAULT_DEPARTMENT_ID = 1001L;

    private final UserMapper userMapper;
    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(
        UserMapper userMapper,
        StudentMapper studentMapper,
        TeacherMapper teacherMapper,
        PasswordEncoder passwordEncoder
    ) {
        this.userMapper = userMapper;
        this.studentMapper = studentMapper;
        this.teacherMapper = teacherMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public PageResponse<UserPageItemVO> getUserPage(UserQueryRequest request) {
        List<UserPageItemVO> list = userMapper.selectUserPage(request);
        long total = userMapper.countUserPage(request);
        return PageResponse.of(list, request.getPageNum(), request.getPageSize(), total);
    }

    @Override
    public UserDetailVO getUserDetail(Long id) {
        UserDetailVO detail = userMapper.selectUserDetailById(id);
        if (detail == null) {
            throw new BusinessException("用户不存在");
        }
        detail.setRoleIds(userMapper.selectRoleIdsByUserId(id));
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(UserCreateRequest request) {
        UserEntity existingUser = userMapper.selectUserEntityByUsername(request.getUsername());
        if (existingUser != null) {
            throw new BusinessException("账号已存在");
        }

        UserEntity entity = new UserEntity();
        entity.setUsername(request.getUsername());
        entity.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        entity.setRealName(request.getRealName());
        entity.setUserType(request.getUserType());
        entity.setDeptId(request.getDeptId() == null ? DEFAULT_DEPARTMENT_ID : request.getDeptId());
        entity.setGender(request.getGender());
        entity.setPhone(request.getPhone());
        entity.setEmail(request.getEmail());
        entity.setStatus(request.getStatus());
        entity.setCreatedBy(SYSTEM_OPERATOR_ID);
        entity.setUpdatedBy(SYSTEM_OPERATOR_ID);
        userMapper.insertUser(entity);
        syncRoles(entity.getId(), request.getRoleIds());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Long id, UserUpdateRequest request) {
        UserEntity entity = userMapper.selectUserEntityById(id);
        if (entity == null) {
            throw new BusinessException("用户不存在");
        }
        entity.setRealName(request.getRealName());
        entity.setUserType(request.getUserType());
        entity.setDeptId(request.getDeptId() == null ? entity.getDeptId() : request.getDeptId());
        entity.setGender(request.getGender());
        entity.setPhone(request.getPhone());
        entity.setEmail(request.getEmail());
        entity.setStatus(request.getStatus());
        entity.setUpdatedBy(SYSTEM_OPERATOR_ID);
        userMapper.updateUser(entity);
        syncRoles(id, request.getRoleIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long id, Integer status) {
        if (userMapper.updateUserStatus(id, status, SYSTEM_OPERATOR_ID) == 0) {
            throw new BusinessException("用户不存在");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetUserPassword(Long id, String password) {
        UserEntity entity = userMapper.selectUserEntityById(id);
        if (entity == null) {
            throw new BusinessException("用户不存在");
        }
        userMapper.updatePasswordById(id, passwordEncoder.encode(password), SYSTEM_OPERATOR_ID);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignUserRoles(Long id, List<Long> roleIds) {
        UserEntity entity = userMapper.selectUserEntityById(id);
        if (entity == null) {
            throw new BusinessException("用户不存在");
        }
        syncRoles(id, roleIds);
    }

    @Override
    public List<UserOptionVO> getUserOptions(UserQueryRequest request) {
        return userMapper.selectUserOptions(request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        studentMapper.clearUserBindingByUserId(id, SYSTEM_OPERATOR_ID);
        teacherMapper.clearUserBindingByUserId(id, SYSTEM_OPERATOR_ID);
        int affectedRows = userMapper.logicDeleteUser(id, SYSTEM_OPERATOR_ID);
        if (affectedRows == 0) {
            throw new BusinessException("用户不存在");
        }
        userMapper.deleteUserRolesByUserId(id);
    }

    private void syncRoles(Long userId, List<Long> roleIds) {
        if (roleIds == null) {
            return;
        }
        userMapper.deleteUserRolesByUserId(userId);
        if (!roleIds.isEmpty()) {
            userMapper.insertUserRoles(userId, SYSTEM_OPERATOR_ID, roleIds);
        }
    }
}
