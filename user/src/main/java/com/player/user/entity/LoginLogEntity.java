package com.player.user.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志实体
 */
@Data
public class LoginLogEntity {

    /** 主键ID（自增） */
    private Long id;

    /** 用户ID */
    private String userId;

    /** 登录IP */
    private String ip;

    /** 登录类型：login/getUserData */
    private String loginType;

    /** 登录时间 */
    private LocalDateTime createTime;
}
