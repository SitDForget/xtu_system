package com.xtu.system.modules.system.log.service;

import com.xtu.system.common.api.PageResponse;
import com.xtu.system.modules.system.log.dto.LoginLogQueryRequest;
import com.xtu.system.modules.system.log.dto.OperationLogQueryRequest;
import com.xtu.system.modules.system.log.entity.LoginLogEntity;
import com.xtu.system.modules.system.log.entity.OperationLogEntity;
import com.xtu.system.modules.system.log.vo.LoginLogPageItemVO;
import com.xtu.system.modules.system.log.vo.OperationLogPageItemVO;

public interface LogService {

    void recordLogin(LoginLogEntity entity);

    void recordOperation(OperationLogEntity entity);

    PageResponse<LoginLogPageItemVO> getLoginLogPage(LoginLogQueryRequest request);

    PageResponse<OperationLogPageItemVO> getOperationLogPage(OperationLogQueryRequest request);
}
