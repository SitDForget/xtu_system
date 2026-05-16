package com.xtu.system.modules.auth.service.impl;

import com.xtu.system.config.security.JwtTokenProvider;
import com.xtu.system.config.security.SecurityUtils;
import com.xtu.system.common.exception.BusinessException;
import com.xtu.system.common.util.RequestContextUtils;
import com.xtu.system.common.util.UserAgentUtils;
import com.xtu.system.modules.auth.dto.LoginRequest;
import com.xtu.system.modules.auth.dto.UpdatePasswordRequest;
import com.xtu.system.modules.auth.service.AuthService;
import com.xtu.system.modules.auth.vo.CurrentUserResponse;
import com.xtu.system.modules.auth.vo.LoginResponse;
import com.xtu.system.modules.system.menu.entity.MenuEntity;
import com.xtu.system.modules.system.menu.mapper.MenuMapper;
import com.xtu.system.modules.system.menu.vo.MenuTreeVO;
import com.xtu.system.modules.system.log.entity.LoginLogEntity;
import com.xtu.system.modules.system.log.service.LogService;
import com.xtu.system.modules.system.user.entity.UserEntity;
import com.xtu.system.modules.system.user.mapper.UserMapper;
import com.xtu.system.modules.system.user.vo.UserDetailVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final MenuMapper menuMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final LogService logService;

    public AuthServiceImpl(
        UserMapper userMapper,
        MenuMapper menuMapper,
        PasswordEncoder passwordEncoder,
        JwtTokenProvider jwtTokenProvider,
        LogService logService
    ) {
        this.userMapper = userMapper;
        this.menuMapper = menuMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.logService = logService;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        HttpServletRequest currentRequest = RequestContextUtils.getCurrentRequest();
        UserEntity user = userMapper.selectUserEntityByUsername(request.getUsername());
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            recordLoginLog(currentRequest, null, request.getUsername(), 0, "账号或密码错误");
            throw new BusinessException("账号或密码错误");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            recordLoginLog(currentRequest, user, request.getUsername(), 0, "账号或密码错误");
            throw new BusinessException("账号或密码错误");
        }

        userMapper.updateLastLoginAt(user.getId());
        recordLoginLog(currentRequest, user, user.getUsername(), 1, null);
        return new LoginResponse(
            jwtTokenProvider.generateToken(user.getId(), user.getUsername()),
            "Bearer",
            jwtTokenProvider.getExpirationSeconds()
        );
    }

    @Override
    public CurrentUserResponse getCurrentUser(String authorizationHeader) {
        Long userId = parseUserId(authorizationHeader);
        UserDetailVO user = userMapper.selectUserDetailById(userId);
        if (user == null) {
            throw new BusinessException(401, "登录状态已失效");
        }

        List<String> roleCodes = userMapper.selectRoleCodesByUserId(userId);
        CurrentUserResponse response = new CurrentUserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());
        response.setUserType(user.getUserType());
        response.setDeptId(user.getDeptId());
        response.setDeptName(user.getDeptName());
        response.setRoles(roleCodes);
        response.setPermissions(menuMapper.selectPermissionCodesByUserId(userId));
        return response;
    }

    @Override
    public List<MenuTreeVO> getMenus(String authorizationHeader) {
        Long userId = parseUserId(authorizationHeader);
        return buildMenuTree(menuMapper.selectMenusByUserId(userId));
    }

    @Override
    public List<String> getPermissions(String authorizationHeader) {
        Long userId = parseUserId(authorizationHeader);
        return menuMapper.selectPermissionCodesByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(UpdatePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("两次输入的新密码不一致");
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();
        UserEntity currentUser = userMapper.selectUserEntityById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException(401, "登录状态已失效");
        }
        if (!passwordEncoder.matches(request.getOldPassword(), currentUser.getPasswordHash())) {
            throw new BusinessException("原密码错误");
        }
        if (passwordEncoder.matches(request.getNewPassword(), currentUser.getPasswordHash())) {
            throw new BusinessException("新密码不能与原密码相同");
        }

        userMapper.updatePasswordById(currentUserId, passwordEncoder.encode(request.getNewPassword()), currentUserId);
    }

    private Long parseUserId(String authorizationHeader) {
        String token = jwtTokenProvider.resolveToken(authorizationHeader);
        return jwtTokenProvider.getAuthenticatedUser(token).getUserId();
    }

    private List<MenuTreeVO> buildMenuTree(List<MenuEntity> entities) {
        Map<Long, MenuTreeVO> nodeMap = new LinkedHashMap<>();
        List<MenuTreeVO> roots = new ArrayList<>();

        for (MenuEntity entity : entities) {
            nodeMap.put(entity.getId(), toTreeNode(entity));
        }

        for (MenuEntity entity : entities) {
            MenuTreeVO node = nodeMap.get(entity.getId());
            if (entity.getParentId() == null || entity.getParentId() == 0) {
                roots.add(node);
                continue;
            }

            MenuTreeVO parent = nodeMap.get(entity.getParentId());
            if (parent == null) {
                roots.add(node);
            } else {
                parent.getChildren().add(node);
            }
        }
        return roots;
    }

    private MenuTreeVO toTreeNode(MenuEntity entity) {
        MenuTreeVO node = new MenuTreeVO();
        node.setId(entity.getId());
        node.setParentId(entity.getParentId());
        node.setMenuName(entity.getMenuName());
        node.setMenuType(entity.getMenuType());
        node.setRoutePath(entity.getRoutePath());
        node.setComponentPath(entity.getComponentPath());
        node.setPermissionCode(entity.getPermissionCode());
        node.setIcon(entity.getIcon());
        node.setSortNo(entity.getSortNo());
        node.setVisible(entity.getVisible());
        node.setStatus(entity.getStatus());
        node.setRemark(entity.getRemark());
        return node;
    }

    private void recordLoginLog(
        HttpServletRequest request,
        UserEntity user,
        String username,
        int loginStatus,
        String failReason
    ) {
        try {
            String userAgent = request == null ? null : request.getHeader("User-Agent");
            LoginLogEntity logEntity = new LoginLogEntity();
            logEntity.setUserId(user == null ? null : user.getId());
            logEntity.setUsername(username);
            logEntity.setRealName(user == null ? null : user.getRealName());
            logEntity.setLoginType("password");
            logEntity.setLoginIp(RequestContextUtils.resolveClientIp(request));
            logEntity.setLoginLocation("本地环境");
            logEntity.setUserAgent(userAgent);
            logEntity.setBrowser(UserAgentUtils.resolveBrowser(userAgent));
            logEntity.setOs(UserAgentUtils.resolveOs(userAgent));
            logEntity.setLoginStatus(loginStatus);
            logEntity.setFailReason(failReason);
            logService.recordLogin(logEntity);
        } catch (Exception ignored) {
        }
    }
}
