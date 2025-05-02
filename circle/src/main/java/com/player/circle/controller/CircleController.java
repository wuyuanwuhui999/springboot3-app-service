package com.player.circle.controller;

import com.player.circle.entity.CircleEntity;
import com.player.circle.service.ICircleService;
import com.player.common.entity.ResultEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/service")
@RestController
public class CircleController {
    @Autowired
    private ICircleService circleService;

    // 获取朋友圈列表
    @GetMapping("/circle/getCircleListByType")
    public ResultEntity getCircleListByType(
            @RequestParam("pageSize") int pageSize,
            @RequestParam("pageNum") int pageNum,
            @RequestParam("type") String type
    ) {
        return circleService.getCircleListByType(pageNum, pageSize, type);
    }

    // 获取文章的评论数量，浏览数量，收藏数量
    @GetMapping("/circle/getCircleArticleCount")
    public ResultEntity getCircleArticleCount(@RequestParam("id") int id) {
        return circleService.getCircleArticleCount(id);
    }

    // 保存图片和文字
    @PostMapping("/circle-getway/insertCircle")
    public ResultEntity saveSay(@RequestBody CircleEntity circleEntity, @RequestHeader("Authorization") String token) {
        return circleService.insertCircle(circleEntity,token);
    }

    // 保存图片和文字
    @GetMapping("/circle-getway/getCircleByLastUpdateTime")
    public ResultEntity getCircleByLastUpdateTime(String lastUpdateTime,String type) {
        return circleService.getCircleByLastUpdateTime(lastUpdateTime,type);
    }
}
