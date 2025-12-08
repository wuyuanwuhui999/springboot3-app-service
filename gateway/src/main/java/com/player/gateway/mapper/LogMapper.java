package com.player.gateway.mapper;

import org.springframework.stereotype.Repository;
import com.player.gateway.entity.LogEntity;
import org.apache.ibatis.annotations.Param;

@Repository
public interface LogMapper {

    /**
     * 插入日志记录
     */
    int insertLog(LogEntity logEntity);

    /**
     * 根据请求ID更新响应信息
     */
    int updateResponseInfo(String requestId, Integer responseStatus, String responseBody, String responseHeaders, Long executeTime, String errorMessage);
}
