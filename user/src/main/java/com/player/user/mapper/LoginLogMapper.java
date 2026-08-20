package com.player.user.mapper;

import com.player.user.entity.LoginLogEntity;
import org.springframework.stereotype.Repository;

/**
 * 登录日志 Mapper
 */
@Repository
public interface LoginLogMapper {

    /**
     * 插入登录日志
     */
    Long insertLoginLog(LoginLogEntity loginLogEntity);
}
