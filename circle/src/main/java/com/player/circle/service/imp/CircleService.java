package com.player.circle.service.imp;

import com.player.circle.entity.CircleEntity;
import com.player.circle.handler.MyWebSocketHandler;
import com.player.circle.mapper.CircleMapper;
import com.player.circle.service.ICircleService;
import com.player.common.entity.ResultEntity;
import com.player.common.entity.ResultUtil;
import com.player.common.entity.UserEntity;
import com.player.common.utils.Common;
import com.player.common.utils.JwtToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
public class CircleService implements ICircleService {
    @Value("${token.secret}")
    private String secret;

    @Value("${static.upload-path}")
    private String uploadPath;

    @Autowired
    private CircleMapper circleMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MyWebSocketHandler webSocketHandler;

    /**
     * @author: wuwenqiang
     * @description: 获取电影圈列表,插入日志表
     * @date: 2022-11-17 23:15
     */
    @Override
    public ResultEntity getCircleListByType(int pageNum, int pageSize, String type) {
        int start = (pageNum - 1) * pageSize;
        List<CircleEntity>circleArticleList = circleMapper.getCircleListByType(start, pageSize, type);
        Long total = circleMapper.getCircleCount(type);
        ResultEntity resultEntity = ResultUtil.success(circleArticleList);
        resultEntity.setTotal(total);
        return resultEntity;
    }

    /**
     * @author: wuwenqiang
     * @description: 获取每条电影圈评论数量和浏览数量和点赞数量
     * @date: 2022-12-03 16:01
     */
    @Override
    public ResultEntity getCircleArticleCount(int id) {
        return ResultUtil.success(circleMapper.getCircleArticleCount(id));
    }

    /**
     * @author: wuwenqiang
     * @description: 获取最近更新的电影
     * @date: 2022-12-03 16:02
     */
    @Override
    public ResultEntity insertCircle(CircleEntity circleEntity, String token){
        if(!StringUtils.isEmpty(circleEntity.getImgs())){
            String[] base64Imgs = circleEntity.getImgs().split(",");
            String imgs = "";
            for(int i = 0; i < base64Imgs.length; i++){
                String base64 = base64Imgs[i];
                String ext = base64.replaceAll(";base64,.+","").replaceAll("data:image/","");
                base64 = base64.replaceAll("data:image/.+base64,","");
                String imgName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
                String newImgName = Common.generateImage(base64, uploadPath+imgName);
                if(newImgName != null){
                    imgs += newImgName + (i == base64Imgs.length - 1 ? "" : ",");
                }
            }
            circleEntity.setImgs(imgs);
        }
        webSocketHandler.broadcastMessage("有一条新消息");
        circleEntity.setUserId(JwtToken.parseToken(token, UserEntity.class,secret).getId());
        return ResultUtil.success(circleMapper.insertCircle(circleEntity));
    }

    /**
     * @author: wuwenqiang
     * @description: 获取朋友圈最近更新的数量
     * @date: 2025-04-20 11:48
     */
    @Override
    public ResultEntity getCircleByLastUpdateTime(String lastUpdateTime,String type){
        return ResultUtil.success(circleMapper.getCircleByLastUpdateTime(lastUpdateTime,type));
    }
}
