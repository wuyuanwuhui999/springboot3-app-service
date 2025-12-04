package com.player.gateway.music.entity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
public class MusicAuthorCategoryEntity {
    @Schema(description = "主键")
    private Long id;//主键

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "排名")
    private int rank;

    @Schema(description = "是否禁用")
    private int disabled;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;
}
