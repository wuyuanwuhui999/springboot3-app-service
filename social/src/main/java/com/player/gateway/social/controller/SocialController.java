package com.player.gateway.social.controller;

import com.player.gateway.common.entity.CommentEntity;
import com.player.gateway.common.entity.LikeEntity;
import com.player.gateway.common.entity.ResultEntity;
import com.player.gateway.social.service.ISocialService;
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
            @RequestHeader("Authorization") String token,
            @RequestBody CommentEntity commentEntity
    ) {
        return socialService.insertComment(token,commentEntity);
    }

    // 删除评论
    @DeleteMapping("/social/deleteComment/{id}")
    public ResultEntity deleteComment(
            @RequestHeader("Authorization") String token,
            @PathVariable("id") int id
    ) {
        return socialService.deleteComment(id,token);
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
            @RequestHeader("Authorization") String token
    ) {
        return socialService.saveLike(likeEntity,token);
    }

    // 删除收藏
    @DeleteMapping("/social/deleteLike")
    public ResultEntity deleteLike(
            @RequestParam("relationId") Long relationId,
            @RequestParam("type") String type,
            @RequestHeader("Authorization") String token
    ) {
        return socialService.deleteLike(relationId,type,token);
    }

    // 查询是否已经收藏
    @GetMapping("/social/isLike")
    public ResultEntity isLike(
            @RequestParam("relationId") Long relationId,
            @RequestParam("type") String type,
            @RequestHeader("Authorization") String token
    ) {
        return socialService.isLike(relationId,type,token);
    }


}
