package com.player.common.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
public class SearchHistory {
    @Schema(description = "主键")
    private Long id;

    @Schema(description = "类型")
    private String type;//类型

    @Schema(description = "用户id")
    private String userId;//用户id

    @Schema(description = "搜索内容")
    private String content;//搜索内容

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "创建时间")
    private Date updateTime;
}
