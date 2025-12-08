package com.player.gateway.mapper;

import com.player.gateway.entity.LogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface LogMapper {

    /**
     * 插入日志记录
     */
    int insertLog(LogEntity logEntity);

    /**
     * 根据请求ID更新响应信息
     */
    int updateResponseInfo(
            @Param("requestId") String requestId,
            @Param("responseStatus") Integer responseStatus,
            @Param("responseBody") String responseBody,
            @Param("responseHeaders") String responseHeaders,
            @Param("executeTime") Long executeTime,
            @Param("errorMessage") String errorMessage
    );
}