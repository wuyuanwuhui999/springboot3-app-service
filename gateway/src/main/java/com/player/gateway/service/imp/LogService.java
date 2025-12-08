package com.player.gateway.service.imp;

import com.player.gateway.entity.LogEntity;
import com.player.gateway.mapper.LogMapper;
import com.player.gateway.service.ILogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class LogService implements ILogService {

    @Autowired
    private LogMapper logMapper;

    /**
     * 保存请求日志
     */
    @Override
    @Transactional
    public void saveRequestLog(LogEntity logEntity) {
        if (logEntity.getId() == null) {
            logEntity.setId(UUID.randomUUID().toString().replace("-", ""));
        }
        if (logEntity.getCreateTime() == null) {
            logEntity.setCreateTime(LocalDateTime.now());
        }
        logMapper.insertLog(logEntity);
    }

    /**
     * 更新响应信息
     */
    @Override
    @Transactional
    public void updateResponseInfo(String requestId, String responseStatus,
                                   String responseBody, String responseHeaders,
                                   Long executeTime, String errorMessage) {
        // 将responseStatus转换为Integer
        Integer status = null;
        try {
            status = Integer.parseInt(responseStatus);
        } catch (NumberFormatException e) {
            status = 500;
        }

        logMapper.updateResponseInfo(requestId, status, responseBody,
                responseHeaders, executeTime, errorMessage);
    }
}
