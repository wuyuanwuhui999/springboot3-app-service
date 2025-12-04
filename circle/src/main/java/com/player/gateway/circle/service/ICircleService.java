package com.player.gateway.circle.service;

import com.player.gateway.circle.entity.CircleEntity;
import com.player.gateway.common.entity.ResultEntity;

public interface ICircleService {

    ResultEntity getCircleListByType(int pageNum, int pageSize, String type);

    ResultEntity getCircleArticleCount(int id);

    ResultEntity insertCircle(CircleEntity circleEntity, String token);

    ResultEntity getCircleByLastUpdateTime(String lastUpdateTime,String type);
}
