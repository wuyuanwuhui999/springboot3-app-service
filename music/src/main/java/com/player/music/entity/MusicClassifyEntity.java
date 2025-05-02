package com.player.music.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
public class MusicClassifyEntity {

    @Schema(description = "主键")
    private int id;

    @Schema(description = "分类名称")
    private String classifyName;

    @Schema(description = "权限")
    private int permission;

    @Schema(description = "分类排名")
    private int classifyRank;

    @Schema(description = "是否禁用")
    private int disabled;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;
}
