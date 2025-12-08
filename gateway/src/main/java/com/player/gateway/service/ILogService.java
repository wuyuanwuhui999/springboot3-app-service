package com.player.gateway.service;

import com.player.gateway.entity.LogEntity;

public interface ILogService {
    public void saveRequestLog(LogEntity logEntity);

    public void updateResponseInfo(String requestId, String responseStatus,
                                   String responseBody, String responseHeaders,
                                   Long executeTime, String errorMessage);
}
