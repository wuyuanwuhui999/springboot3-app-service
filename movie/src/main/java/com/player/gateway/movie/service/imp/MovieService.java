package com.player.gateway.movie.service.imp;

import com.alibaba.fastjson2.JSON;
import com.player.gateway.common.entity.ResultEntity;
import com.player.gateway.common.entity.ResultUtil;

import com.player.gateway.movie.mapper.MovieMapper;
import com.player.gateway.movie.service.IMovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Service
public class MovieService implements IMovieService {

    @Autowired
    private MovieMapper movieMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * @author: wuwenqiang
     * @description: 查询电影分类
     * @date: 2020-12-24 22:40
     */
    @Override
    public ResultEntity findClassify(String redisKey) {
        String result = (String) redisTemplate.opsForValue().get(redisKey);
        if(!StringUtils.isEmpty(result)){
            ResultEntity resultEntity= JSON.parseObject(result,ResultEntity.class);
            return resultEntity;
        }else{
            ResultEntity resultEntity = ResultUtil.success(movieMapper.findClassify());
            redisTemplate.opsForValue().set(redisKey, JSON.toJSONString(resultEntity),1, TimeUnit.DAYS);
            return resultEntity;
        }
    }

    /**
     * @author: wuwenqiang
     * @description: 获取推荐的影片
     * @date: 2020-12-25 00:04
     */
    @Override
    public ResultEntity getKeyWord(String classify,String url) {
        String result = (String) redisTemplate.opsForValue().get(url);
        if(!StringUtils.isEmpty(result)){
            ResultEntity resultEntity= JSON.parseObject(result,ResultEntity.class);
            return resultEntity;
        }else{
            ResultEntity resultEntity = ResultUtil.success(movieMapper.getKeyWord(classify));
            redisTemplate.opsForValue().set(url, JSON.toJSONString(resultEntity),1, TimeUnit.DAYS);
            return resultEntity;
        }
    }

    /**
     * @author: wuwenqiang
     * @description: 获取用户访问记录
     * @date: 2020-12-21 22:40
     */
    @Override
    public ResultEntity getUserMsg(String userId) {
        return ResultUtil.success(movieMapper.getUserMsg(userId));
    }

    /**
     * @author: wuwenqiang
     * @description: 按classify大类查询所有catory小类
     * @date: 2020-12-25 22:29
     */
    @Override
    public ResultEntity getAllCategoryByClassify(String classsify,String url) {
        String result = (String) redisTemplate.opsForValue().get(url);
        if(!StringUtils.isEmpty(result)){
            ResultEntity resultEntity= JSON.parseObject(result,ResultEntity.class);
            return resultEntity;
        }else{
            ResultEntity resultEntity = ResultUtil.success(movieMapper.getAllCategoryByClassify(classsify));
            redisTemplate.opsForValue().set(url, JSON.toJSONString(resultEntity),1, TimeUnit.DAYS);
            return resultEntity;
        }
    }

    /**
     * @author: wuwenqiang
     * @description: 按页面获取要展示的category小类
     * @date: 2020-12-25 22:29
     */
    @Override
    public ResultEntity getAllCategoryListByPageName(String pageName,String url) {
        String result = (String) redisTemplate.opsForValue().get(url);
        if(!StringUtils.isEmpty(result)){
            ResultEntity resultEntity= JSON.parseObject(result,ResultEntity.class);
            return resultEntity;
        }else{
            ResultEntity resultEntity =ResultUtil.success(movieMapper.getAllCategoryListByPageName(pageName));
            redisTemplate.opsForValue().set(url, JSON.toJSONString(resultEntity),1, TimeUnit.DAYS);
            return resultEntity;
        }
    }

    /**
     * @author: wuwenqiang
     * @description: 获取大类中的小类
     * @date: 2020-12-25 22:29
     */
    @Override
    public ResultEntity getCategoryList(String classify, String category,String url) {
        String result = (String) redisTemplate.opsForValue().get(url);
        if(!StringUtils.isEmpty(result)){
            ResultEntity resultEntity= JSON.parseObject(result,ResultEntity.class);
            return resultEntity;
        }else{
            ResultEntity resultEntity =  ResultUtil.success(movieMapper.getCategoryList(classify, category));
            redisTemplate.opsForValue().set(url, JSON.toJSONString(resultEntity),1, TimeUnit.DAYS);
            return resultEntity;
        }
    }

    /**
     * @author: wuwenqiang
     * @description: 根据分类获取前20条数据
     * @date: 2021-12-21 23:36
     */
    @Override
    public ResultEntity getTopMovieList(String classify, String category,String url) {
        String result = (String) redisTemplate.opsForValue().get(url);
        if(!StringUtils.isEmpty(result)){
            ResultEntity resultEntity= JSON.parseObject(result,ResultEntity.class);
            return resultEntity;
        }else{
            ResultEntity resultEntity =  ResultUtil.success(movieMapper.getTopMovieList(classify, category));
            redisTemplate.opsForValue().set(url, JSON.toJSONString(resultEntity),1, TimeUnit.DAYS);
            return resultEntity;
        }
    }

    /**
     * @author: wuwenqiang
     * @description: 搜索
     * @date: 2020-12-25 22:29
     */
    @Override
    public ResultEntity search(String classify, String category, String label,String star,String director,String keyword,int pageNum,int pageSize,String redisKey) {
        String result = (String) redisTemplate.opsForValue().get(redisKey);
        if(!StringUtils.isEmpty(result)){
            ResultEntity resultEntity= JSON.parseObject(result,ResultEntity.class);
            return resultEntity;
        }else{
            if(pageSize > 500)pageSize = 500;
            int start = (pageNum - 1) * pageSize;
            ResultEntity resultEntity =  ResultUtil.success(movieMapper.search(classify, category, label,star,director,keyword,start,pageSize));
            Long total = movieMapper.searchTotal(classify, category, label,star,director,keyword);
            resultEntity.setTotal(total);
            redisTemplate.opsForValue().set(redisKey, JSON.toJSONString(resultEntity),1, TimeUnit.DAYS);
            return resultEntity;
        }
    }


    /**
     * @author: wuwenqiang
     * @description: 获取演员
     * @date: 2020-12-26 10:41
     */
    @Override
    public ResultEntity getStar(Long movieId,String redisKey) {
        redisKey += "?movieId=" + movieId;
        String result = (String) redisTemplate.opsForValue().get(redisKey);
        if(!StringUtils.isEmpty(result)){
            ResultEntity resultEntity= JSON.parseObject(result,ResultEntity.class);
            return resultEntity;
        }else{
            ResultEntity resultEntity =  ResultUtil.success(movieMapper.getStar(movieId));
            redisTemplate.opsForValue().set(redisKey, JSON.toJSONString(resultEntity),1, TimeUnit.DAYS);
            return resultEntity;
        }
    }

    /**
     * @author: wuwenqiang
     * @description: 获取播放地址
     * @date: 2020-12-26 10:41
     */
    @Override
    public ResultEntity getMovieUrl(Long movieId,String redisKey) {
        redisKey += "?movieId=" + movieId;
        String result = (String) redisTemplate.opsForValue().get(redisKey);
        if(!StringUtils.isEmpty(result)){
            ResultEntity resultEntity= JSON.parseObject(result,ResultEntity.class);
            return resultEntity;
        }else{
            ResultEntity resultEntity =  ResultUtil.success(movieMapper.getMovieUrl(movieId));
            redisTemplate.opsForValue().set(redisKey, JSON.toJSONString(resultEntity),1, TimeUnit.DAYS);
            return resultEntity;
        }
    }

    /**
     * @author: wuwenqiang
     * @description: 获取播放记录
     * @date: 2021-02-28 12:08
     */
    @Override
    public ResultEntity getPlayRecord(String userId,int pageNum,int pageSize) {
        int start = (pageNum - 1) * pageSize;
        return ResultUtil.success(movieMapper.getPlayRecord(userId,start,pageSize));
    }

    /**
     * @author: wuwenqiang
     * @description: 保存播放记录
     * @date: 2020-12-25 22:29
     */
    @Override
    @Transactional
    public ResultEntity savePlayRecord(int movieId,String userId) {
        return ResultUtil.success(movieMapper.savePlayRecord(movieId,userId));
    }

    /**
     * @author: wuwenqiang
     * @description: 获取浏览记录
     * @date: 2021-08-24 21:59
     */
    @Override
    public ResultEntity getViewRecord(String userId,int pageNum,int pageSize){
        if(pageSize > 500) pageSize = 500;
        int start = (pageNum - 1)*pageSize;
        return ResultUtil.success(movieMapper.getViewRecord(userId,start,pageSize));
    }

    /**
     * @author: wuwenqiang
     * @description: 保存浏览记录
     * @date: 2020-12-25 22:29
     */
    @Override
    @Transactional
    public ResultEntity saveViewRecord(int movieId,String userId) {
        ResultEntity resultEntity = ResultUtil.success(movieMapper.saveViewRecord(movieId,userId));
        return resultEntity;
    }

    /**
     * @author: wuwenqiang
     * @description: 查询收藏列表
     * @date: 2020-12-25 22:29
     */
    @Override
    public ResultEntity getFavoriteList(String userId,int pageNum,int pageSize) {
        if(pageSize > 500) pageSize = 500;
        int start = (pageNum - 1)*pageSize;
        return ResultUtil.success(movieMapper.getFavoriteList(userId,start,pageSize));
    }

    /**
     * @author: wuwenqiang
     * @description: 添加收藏
     * @date: 2020-12-25 22:29
     */
    @Override
    public ResultEntity saveFavorite(int movieId, String userId) {
        return ResultUtil.success(movieMapper.saveFavorite(movieId,userId));
    }

    /**
     * @author: wuwenqiang
     * @description: 删除收藏
     * @date: 2021-03-07 16:10
     */
    @Override
    public ResultEntity deleteFavorite(int movieId,String userId) {
        return ResultUtil.success(movieMapper.deleteFavorite(movieId,userId));
    }

    @Override
    public ResultEntity isFavorite(Long movieId,String userId) {
        return ResultUtil.success(movieMapper.isFavorite(movieId,userId));
    }

    @Override
    public ResultEntity getYourLikes(String labels,String classify,String redisKey) {
        redisKey += "?labels=" + labels;
        String result = (String) redisTemplate.opsForValue().get(redisKey);
        if(!StringUtils.isEmpty(result)){
            ResultEntity resultEntity= JSON.parseObject(result,ResultEntity.class);
            return resultEntity;
        }else{
            labels = labels.replaceAll("^/|/$","");
            String[] myLabels = labels.split("/");
            ResultEntity resultEntity = ResultUtil.success(movieMapper.getYourLikes(myLabels,classify));
            redisTemplate.opsForValue().set(redisKey, JSON.toJSONString(resultEntity),1, TimeUnit.DAYS);
            return resultEntity;
        }
    }

    @Override
    public ResultEntity getRecommend(String classify,String redisKey) {
        redisKey += "?classify=" + classify;
        String result = (String) redisTemplate.opsForValue().get(redisKey);
        if(!StringUtils.isEmpty(result)){
            ResultEntity resultEntity = JSON.parseObject(result,ResultEntity.class);
            return resultEntity;
        }else{
            ResultEntity resultEntity = ResultUtil.success(movieMapper.getRecommend(classify));
            redisTemplate.opsForValue().set(redisKey, JSON.toJSONString(resultEntity),1, TimeUnit.DAYS);
            return resultEntity;
        }
    }

    /**
     * @author: wuwenqiang
     * @description: 获取电影详情
     * @date: 2021-08-25 22:22
     */
    @Override
    public ResultEntity getMovieDetail(int movieId){
        return ResultUtil.success(movieMapper.getMovieDetail(movieId));
    }


    /**
     * @author: wuwenqiang
     * @description: 按类型获取电影
     * @date: 2021-08-28 11:43
     */
    @Override
    public ResultEntity getMovieListByType(String types,String classify,String redisKey) {
        redisKey += "?classify="+classify+"&types=" + types;
        String result = (String) redisTemplate.opsForValue().get(redisKey);
        if(!StringUtils.isEmpty(result)){
            ResultEntity resultEntity= JSON.parseObject(result,ResultEntity.class);
            return resultEntity;
        }else{
            String[] myTypes = types.split(" ");
            ResultEntity resultEntity = ResultUtil.success(movieMapper.getMovieListByType(myTypes,classify));
            redisTemplate.opsForValue().set(redisKey, JSON.toJSONString(resultEntity),1, TimeUnit.DAYS);
            return resultEntity;
        }
    }

    /**
     * @author: wuwenqiang
     * @description: 查询收藏列表
     * @date: 2020-12-25 22:29
     */
    @Override
    public ResultEntity getSearchHistory(String userId,int pageNum,int pageSize) {
        if(pageSize > 500) pageSize = 500;
        int start = (pageNum - 1)*pageSize;
        ResultEntity resultEntity = ResultUtil.success(movieMapper.getSearchHistory(userId, start, pageSize));
        resultEntity.setTotal(movieMapper.getSearchHistoryTotal(userId));
        return resultEntity;
    }
}
