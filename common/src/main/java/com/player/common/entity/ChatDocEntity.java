package com.player.common.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
public class ChatDocEntity {
    @Schema(description = "主键")
    private String id;
    @Schema(description = "租户id")
    private String appId;
    @Schema(description = "文件名称")
    private String name;
    @Schema(description = "文件格式")
    private String ext;
    @Schema(description = "用户id")
    private String userId;
    @Schema(description = "创建时间")
    private Date createTime;
    @Schema(description = "更新时间")
    private Date updateTime;
}
