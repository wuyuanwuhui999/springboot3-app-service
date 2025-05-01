package com.player.common.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.util.Date;
import java.util.List;

@Data
@ToString
public class CommentEntity {
    @Schema(description = "主键")
    private Long id;//主键

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "父节点id")
    private Long parentId;

    @Schema(description = "顶级节点id")
    private Long topId;

    @Schema(description = "关联的影片id")
    private Long relationId;

    @Schema(description = "类型，movie，aiqiyi，article")
    private String type;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;

    @Schema(description = "回复数量")
    private int replyCount;

    @Schema(description = "用户id")
    private String userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户头像")
    private String avater;

    @Schema(description = "被回复者id")
    private String replyUserId;

    @Schema(description = "被回复者名称")
    private String replyUserName;

    @Schema(description = "回复者列表")
    private List<CommentEntity> replyList;
}
