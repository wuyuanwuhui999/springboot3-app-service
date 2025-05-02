package com.player.music.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
public class MusicAuthorEntity {
    @Schema(description = "主键")
    private int id;//主键

    @Schema(description = "歌手id")
    private String authorId;

    @Schema(description = "歌手名称")
    private String authorName;

    @Schema(description = "歌手头像")
    private String avatar;

    @Schema(description = "总数")
    private int total;

    @Schema(description = "是否喜欢")
    private int isLike;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;
}
