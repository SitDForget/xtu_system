package com.xtu.system.modules.system.log.service.impl;

import com.xtu.system.common.api.PageResponse;
import com.xtu.system.modules.system.log.dto.LoginLogQueryRequest;
import com.xtu.system.modules.system.log.dto.OperationLogQueryRequest;
import com.xtu.system.modules.system.log.entity.LoginLogEntity;
import com.xtu.system.modules.system.log.entity.OperationLogEntity;
import com.xtu.system.modules.system.log.mapper.LogMapper;
import com.xtu.system.modules.system.log.service.LogService;
import com.xtu.system.modules.system.log.vo.LoginLogPageItemVO;
import com.xtu.system.modules.system.log.vo.OperationLogPageItemVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogServiceImpl implements LogService {

    private final LogMapper logMapper;

    public LogServiceImpl(LogMapper logMapper) {
        this.logMapper = logMapper;
    }

    @Override
    public void recordLogin(LoginLogEntity entity) {
        logMapper.insertLoginLog(entity);
    }

    @Override
    public void recordOperation(OperationLogEntity entity) {
        logMapper.insertOperationLog(entity);
    }

    @Override
    public PageResponse<LoginLogPageItemVO> getLoginLogPage(LoginLogQueryRequest request) {
        List<LoginLogPageItemVO> list = logMapper.selectLoginLogPage(request);
        long total = logMapper.countLoginLogPage(request);
        return PageResponse.of(list, request.getPageNum(), request.getPageSize(), total);
    }

    @Override
    public PageResponse<OperationLogPageItemVO> getOperationLogPage(OperationLogQueryRequest request) {
        List<OperationLogPageItemVO> list = logMapper.selectOperationLogPage(request);
        long total = logMapper.countOperationLogPage(request);
        return PageResponse.of(list, request.getPageNum(), request.getPageSize(), total);
    }
}
