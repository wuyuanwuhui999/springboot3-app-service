package com.player.social.controller;

import com.player.common.entity.CommentEntity;
import com.player.common.entity.LikeEntity;
import com.player.common.entity.ResultEntity;
import com.player.social.service.ISocialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/service")
@RestController
public class SocialController {
    @Autowired
    private ISocialService socialService;

    // 获取总评论数量
    @GetMapping("/social/getCommentCount")
    public ResultEntity getCommentCount(
            @RequestParam("relationId") int relationId,
            @RequestParam("type") String type
    ) {
        return socialService.getCommentCount(relationId,type);
    }

    // 获取一级评论列表
    @GetMapping("/social/getTopCommentList")
    public ResultEntity getTopCommentList(
            @RequestParam("relationId") int relationId,
            @RequestParam("type") String type,
            @RequestParam("pageNum") int pageNum,
            @RequestParam("pageSize")int pageSize
    ) {
        return socialService.getTopCommentList(relationId,type,pageNum,pageSize);
    }

    // 新增评论
    @PostMapping("/social/insertComment")
    public ResultEntity insertComment(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody CommentEntity commentEntity
    ) {
        return socialService.insertComment(userId,commentEntity);
    }

    // 删除评论
    @DeleteMapping("/social/deleteComment/{id}")
    public ResultEntity deleteComment(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable("id") int id
    ) {
        return socialService.deleteComment(id,userId);
    }

    // 获取回复列表
    @GetMapping("/social/getReplyCommentList")
    public ResultEntity getReplyCommentList(
            @RequestParam("topId") int topId,
            @RequestParam("pageNum") int pageNum,
            @RequestParam("pageSize")int pageSize
    ) {
        return socialService.getReplyCommentList(topId,pageNum,pageSize);
    }

    // 保存收藏记录
    @PostMapping("/social/saveLike")
    public ResultEntity saveLike(
            @RequestBody LikeEntity likeEntity,
            @RequestHeader("X-User-Id") String userId
    ) {
        return socialService.saveLike(likeEntity,userId);
    }

    // 删除收藏
    @DeleteMapping("/social/deleteLike")
    public ResultEntity deleteLike(
            @RequestParam("relationId") Long relationId,
            @RequestParam("type") String type,
            @RequestHeader("X-User-Id") String userId
    ) {
        return socialService.deleteLike(relationId,type,userId);
    }

    // 查询是否已经收藏
    @GetMapping("/social/isLike")
    public ResultEntity isLike(
            @RequestParam("relationId") Long relationId,
            @RequestParam("type") String type,
            @RequestHeader("X-User-Id") String userId
    ) {
        return socialService.isLike(relationId,type,userId);
    }


}
