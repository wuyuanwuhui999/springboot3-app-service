package com.player.gateway.circle.mapper;

import com.player.gateway.circle.entity.CircleEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface CircleMapper {

    List<CircleEntity> getCircleListByType(int start, int pageSize,String type);

    Map<String,Integer> getCircleArticleCount(int id);

    Long getCircleCount(String type);

    Integer insertCircle(CircleEntity circleEntity);

    Long getCircleByLastUpdateTime(String lastUpdateTime,String type);
}
