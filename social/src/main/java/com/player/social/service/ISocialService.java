package com.player.social.service;

import com.player.common.entity.CommentEntity;
import com.player.common.entity.LikeEntity;
import com.player.common.entity.ResultEntity;

public interface ISocialService {

    ResultEntity getTopCommentList(int movieId, String type, int pageNum, int pageSize);

    ResultEntity insertComment(String token, CommentEntity commentEntity);

    ResultEntity deleteComment(int id, String userId);

    ResultEntity getReplyCommentList(int topId, int pageNum, int pageSize);

    ResultEntity saveLike(LikeEntity likeEntity, String token);

    ResultEntity deleteLike(Long relationId,String type,String token);

    ResultEntity isLike(Long relationId,String type,String token);

    ResultEntity getCommentCount(int relationId, String type);
}
