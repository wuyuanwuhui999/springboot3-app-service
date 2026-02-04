package com.player.gateway.service;

import com.player.gateway.entity.LogEntity;

public interface ILogService {
    void saveRequestLog(LogEntity logEntity);
    void updateResponseInfo(String requestId, String responseStatus, String responseBody,
                            String responseHeaders, Long executeTime, String errorMessage);
}