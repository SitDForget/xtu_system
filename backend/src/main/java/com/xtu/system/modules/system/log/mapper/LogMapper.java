package com.xtu.system.modules.system.log.mapper;

import com.xtu.system.modules.system.log.dto.LoginLogQueryRequest;
import com.xtu.system.modules.system.log.dto.OperationLogQueryRequest;
import com.xtu.system.modules.system.log.entity.LoginLogEntity;
import com.xtu.system.modules.system.log.entity.OperationLogEntity;
import com.xtu.system.modules.system.log.vo.LoginLogPageItemVO;
import com.xtu.system.modules.system.log.vo.OperationLogPageItemVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LogMapper {

    int insertLoginLog(LoginLogEntity entity);

    List<LoginLogPageItemVO> selectLoginLogPage(LoginLogQueryRequest request);

    long countLoginLogPage(LoginLogQueryRequest request);

    int insertOperationLog(OperationLogEntity entity);

    List<OperationLogPageItemVO> selectOperationLogPage(OperationLogQueryRequest request);

    long countOperationLogPage(OperationLogQueryRequest request);
}
