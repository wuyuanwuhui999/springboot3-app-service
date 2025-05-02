package com.player.music.service.imp;

import com.alibaba.fastjson2.JSON;
import com.player.common.entity.ResultEntity;
import com.player.common.entity.ResultUtil;
import com.player.common.entity.UserEntity;
import com.player.common.utils.JwtToken;
import com.player.music.entity.MusicFavoriteDirectoryEntity;
import com.player.music.entity.MusicFavoriteEntity;
import com.player.music.entity.MusicRecordEntity;
import com.player.music.mapper.MusicMapper;
import com.player.music.service.IMusicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class MusiceService implements IMusicService {

    @Value("${token.secret}")
    private String secret;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MusicMapper musicMapper;

    /**
     * @author: wuwenqiang
     * @methodsName: getKeywordMusic
     * @description: 获取搜索框中推荐的音乐
     * @return: String
     * @date: 2023-05-25 20:55
     */
    @Override
    public ResultEntity getKeywordMusic(String redisKey) {
        String result = (String) redisTemplate.opsForValue().get(redisKey);
        if (!StringUtils.isEmpty(result)) {
            return JSON.parseObject(result, ResultEntity.class);
        } else {
            ResultEntity resultEntity = ResultUtil.success(musicMapper.getKeywordMusic());
            // 修改后的代码
            redisTemplate.opsForValue().set(
                    redisKey,
                    JSON.toJSONString(resultEntity),
                    1,
                    TimeUnit.DAYS
            );
            return resultEntity;
        }
    }

    /**
     * @author: wuwenqiang
     * @methodsName: getMusicClassify
     * @description: 获取推荐音乐列表
     * @return: String
     * @date: 2023-05-25 21:00
     */
    @Override
    public ResultEntity getMusicClassify(String redisKey) {
        String result = (String) redisTemplate.opsForValue().get(redisKey);
        if (!StringUtils.isEmpty(result)) {
            return JSON.parseObject(result, ResultEntity.class);
        } else {
            ResultEntity resultEntity = ResultUtil.success(musicMapper.getMusicClassify());
            // 修改后的代码
            redisTemplate.opsForValue().set(
                    redisKey,
                    JSON.toJSONString(resultEntity),
                    1,
                    TimeUnit.DAYS
            );
            return resultEntity;
        }
    }

    /**
     * @author: wuwenqiang
     * @methodsName: getMusicListByClassifyId
     * @description: 获取推荐音乐列表
     * @return: String
     * @date: 2023-05-25 21:00
     */
    @Override
    public ResultEntity getMusicListByClassifyId(String redisKey, int classifyId, int pageNum, int pageSize, boolean isRedis, String token) {
        String uid = "";
        if (!StringUtils.isEmpty(token)) {
            uid = JwtToken.parseToken(token, UserEntity.class,secret).getId();
        }
        if(isRedis){
            redisKey += "?classifyId=" + classifyId + "&pageNum=" + pageNum + "&pageSize=" + pageNum + "&uid=" + uid;
            String result = (String) redisTemplate.opsForValue().get(redisKey);
            if (!StringUtils.isEmpty(result)) {// 如果缓存中有数据
                return JSON.parseObject(result, ResultEntity.class);
            } else {// 如果缓存中没有数据，从数据库中查询，并将查询结果写入缓存
                ResultEntity resultEntity = findMusicListByClassifyId(classifyId, pageNum, pageSize, uid);
                // 修改后的代码
                redisTemplate.opsForValue().set(
                        redisKey,
                        JSON.toJSONString(resultEntity),
                        1,
                        TimeUnit.DAYS
                );
                return resultEntity;
            }
        }else{
            return findMusicListByClassifyId(classifyId, pageNum, pageSize,uid);
        }
    }

    private ResultEntity findMusicListByClassifyId(int classifyId, int pageNum, int pageSize,String userId){
        if (pageSize > 500) pageSize = 500;
        int start = (pageNum - 1) * pageSize;
        ResultEntity resultEntity = ResultUtil.success(musicMapper.getMusicListByClassifyId(classifyId, start, pageSize, userId));
        Long musicTotalByClassifyId = musicMapper.getMusicTotalByClassifyId(classifyId);
        resultEntity.setTotal(musicTotalByClassifyId);
        return resultEntity;
    }

    @Override
    public ResultEntity getMusicAuthorListByCategoryId(String redisKey,String token,int categoryId, int pageNum, int pageSize) {
        String uid = "";
        if (!StringUtils.isEmpty(token)) {
            uid = JwtToken.parseToken(token, UserEntity.class,secret).getId();
        }
        if (pageSize > 500) pageSize = 500;
        int start = (pageNum - 1) * pageSize;
        ResultEntity resultEntity = ResultUtil.success(musicMapper.getMusicAuthorListByCategoryId(uid,categoryId, start, pageSize));
        Long singerTotal = musicMapper.getMusicAuthorTotal(categoryId);
        resultEntity.setTotal(singerTotal);
        return resultEntity;
    }

    @Override
    public ResultEntity getMusicListByAuthorId(String redisKey,String token,int authorId, int pageNum, int pageSize) {
        String uid = "";
        if (!StringUtils.isEmpty(token)) {
            uid = JwtToken.parseToken(token, UserEntity.class,secret).getId();
        }
        redisKey += "?pageNum=" + pageNum + "&pageSize=" + pageSize + "&authorId=" + authorId;
        String result = (String) redisTemplate.opsForValue().get(redisKey);
        if (!StringUtils.isEmpty(result)) {
            return JSON.parseObject(result, ResultEntity.class);
        } else {
            if (pageSize > 500) pageSize = 500;
            int start = (pageNum - 1) * pageSize;
            ResultEntity resultEntity = ResultUtil.success(musicMapper.getMusicListByAuthorId(uid,authorId, start, pageSize));
            Long singerTotal = musicMapper.getMusicListByAuthorIdTotal(authorId);
            resultEntity.setTotal(singerTotal);
            return resultEntity;
        }
    }


    @Override
    public ResultEntity getFavoriteAuthor(String token,int pageNum, int pageSize){
        UserEntity userEntity = JwtToken.parseToken(token, UserEntity.class,secret);
        if (pageSize > 500) pageSize = 500;
        int start = (pageNum - 1) * pageSize;
        ResultEntity resultEntity = ResultUtil.success(musicMapper.getFavoriteAuthor(userEntity.getId(),start,pageSize));
        Long mySingerCount = musicMapper.getFavoriteAuthorCount(userEntity.getId());
        resultEntity.setTotal(mySingerCount);
        return resultEntity;
    }

    @Override
    public ResultEntity insertFavoriteAuthor(String token,int authorId){
        UserEntity userEntity = JwtToken.parseToken(token, UserEntity.class,secret);
        return ResultUtil.success(musicMapper.insertFavoriteAuthor(userEntity.getId(),authorId));
    }

    @Override
    public ResultEntity deleteFavoriteAuthor(String token,int authorId){
        UserEntity userEntity = JwtToken.parseToken(token, UserEntity.class,secret);
        return ResultUtil.success(musicMapper.deleteFavoriteAuthor(userEntity.getId(),authorId));
    }

    @Override
    public ResultEntity getMusicRecord(String token, int pageNum, int pageSize){
        UserEntity userEntity = JwtToken.parseToken(token, UserEntity.class,secret);
        if (pageSize > 500) pageSize = 500;
        int start = (pageNum - 1) * pageSize;
        ResultEntity resultEntity = ResultUtil.success(musicMapper.getMusicRecord(userEntity.getId(),start,pageSize));
        Long mySingerCount = musicMapper.getMusicRecordCount(userEntity.getId());
        resultEntity.setTotal(mySingerCount);
        return resultEntity;
    }

    /**
     * @author: wuwenqiang
     * @methodsName: insertMusicRecord
     * @description: 插入播放记录
     * @return: ResultEntity
     * @date: 2023-11-20 21:52
     */
    @Override
    public ResultEntity insertMusicRecord(String token, MusicRecordEntity musicRecordEntity){
        musicRecordEntity.setUserId(JwtToken.parseToken(token, UserEntity.class,secret).getId());
        return ResultUtil.success(musicMapper.insertMusicRecord(musicRecordEntity));
    }

    /**
     * @author: wuwenqiang
     * @methodsName: insertMusicLike
     * @description: 插入播放记录
     * @return: ResultEntity
     * @date: 2024-01-05 21:50
     */
    @Override
    public ResultEntity insertMusicLike(String token, int musicId){
        return ResultUtil.success(musicMapper.insertMusicLike(JwtToken.parseToken(token, UserEntity.class,secret).getId(), musicId));
    }

    /**
     * @author: wuwenqiang
     * @methodsName: deleteMusicLike
     * @description: 删除收藏
     * @return: ResultEntity
     * @date: 2024-01-05 21:50
     */
    @Override
    public ResultEntity deleteMusicLike(String token, int id){
        return ResultUtil.success(musicMapper.deleteMusicLike(JwtToken.parseToken(token, UserEntity.class,secret).getId(),id));
    }

    /**
     * @author: wuwenqiang
     * @methodsName: getMusicLike
     * @description: 查询收藏
     * @return: ResultEntity
     * @date: 2024-01-05 21:50
     */
    @Override
    public ResultEntity getMusicLike(String token, int pageNum, int pageSize){
        if (pageSize > 500) pageSize = 500;
        int start = (pageNum - 1) * pageSize;
        ResultEntity resultEntity = ResultUtil.success(musicMapper.getMusicLike(JwtToken.parseToken(token, UserEntity.class,secret).getId(),start,pageSize));
        Long mySingerCount = musicMapper.getMusicLikeCount(JwtToken.parseToken(token, UserEntity.class,secret).getId());
        resultEntity.setTotal(mySingerCount);
        return resultEntity;
    }

    /**
     * @author: wuwenqiang
     * @methodsName: getMusicLike
     * @description: 查询收藏
     * @return: ResultEntity
     * @date: 2024-01-27 16:57
     */
    @Override
    public ResultEntity searchMusic(String token, String keyword, int pageNum, int pageSize){
        String uid = null;
        if(!StringUtils.isEmpty(token)){
            UserEntity userEntity =  JwtToken.parseToken(token, UserEntity.class,secret);
            if(userEntity != null){
                uid = userEntity.getId();
            }
        }
        if (pageSize > 500) pageSize = 500;
        int start = (pageNum - 1) * pageSize;
        ResultEntity resultEntity = ResultUtil.success(musicMapper.searchMusic(uid,keyword,start,pageSize));
        Long mySingerCount = musicMapper.searchMusicCount(keyword);
        resultEntity.setTotal(mySingerCount);
        return resultEntity;
    }

    /**
     * @author: wuwenqiang
     * @methodsName: getMusicAuthorCategory
     * @description: 查询歌手分类
     * @return: ResultEntity
     * @date: 2024-01-27 16:57
     */
    @Override
    public ResultEntity getMusicAuthorCategory(String redisKey){
        String result = (String) redisTemplate.opsForValue().get(redisKey);
        if (!StringUtils.isEmpty(result)) {
            return JSON.parseObject(result, ResultEntity.class);
        } else {
            ResultEntity resultEntity = ResultUtil.success(musicMapper.getMusicAuthorCategory());
            // 修改后的代码
            redisTemplate.opsForValue().set(
                    redisKey,
                    JSON.toJSONString(resultEntity),
                    1,
                    TimeUnit.DAYS
            );
            return resultEntity;
        }
    }

    /**
     * @author: wuwenqiang
     * @methodsName: getFavoriteDirectory
     * @description: 获取收藏夹目录
     * @return: ResultEntity
     * @date: 2024-06-2 11:14
     */
    @Override
    public ResultEntity getFavoriteDirectory(String token,Long musicId) {
        List<MusicFavoriteDirectoryEntity> favoriteDirectory = musicMapper.getFavoriteDirectory(JwtToken.parseToken(token, UserEntity.class,secret).getId(), musicId);
        return ResultUtil.success(favoriteDirectory);
    }

    /**
     * @author: wuwenqiang
     * @methodsName: getMusicListByFavoriteId
     * @description: 根据收藏夹id查询音乐列表
     * @return: ResultEntity
     * @date: 2024-06-2 11:15
     */
    @Override
    public ResultEntity getMusicListByFavoriteId(String token,Long favoriteId,int pageNum,int pageSize) {
        ResultEntity resultEntity = ResultUtil.success(musicMapper.getMusicListByFavoriteId(JwtToken.parseToken(token, UserEntity.class,secret).getId(),favoriteId,(pageNum - 1) * pageSize,pageSize));
        resultEntity.setTotal(musicMapper.getMusicCountByFavoriteId(favoriteId));
        return resultEntity;
    }

    /**
     * @author: wuwenqiang
     * @methodsName: insertFavoriteDirectory
     * @description: 创建音乐收藏夹
     * @return: ResultEntity
     * @date: 2024-06-2 11:15
     */
    @Override
    public ResultEntity insertFavoriteDirectory(String token, MusicFavoriteDirectoryEntity favoriteDirectoryEntity) {
        String uid = JwtToken.parseToken(token, UserEntity.class,secret).getId();
        favoriteDirectoryEntity.setUserId(uid);
        musicMapper.insertFavoriteDirectory(favoriteDirectoryEntity);
        return ResultUtil.success(musicMapper.getFavoriteDirectoryById(favoriteDirectoryEntity.getId()));
    }

    /**
     * @author: wuwenqiang
     * @methodsName: deleteFavoriteDirectory
     * @description: 删除音乐收藏夹
     * @return: ResultEntity
     * @date: 2024-06-2 11:16
     */
    @Transactional
    @Override
    public ResultEntity deleteFavoriteDirectory(String token, Long favoriteId) {
        String id = JwtToken.parseToken(token, UserEntity.class, secret).getId();
        musicMapper.deleteMusicFavoriteByFavoriteId(id,favoriteId);
        return ResultUtil.success(musicMapper.deleteFavoriteDirectory(id,favoriteId));
    }

    /**
     * @author: wuwenqiang
     * @methodsName: updateFavoriteDirectory
     * @description: 更改音乐收藏夹名称
     * @return: ResultEntity
     * @date: 2024-06-2 11:16
     */
    @Override
    public ResultEntity updateFavoriteDirectory(String token, Long favoriteId,String name) {
        return ResultUtil.success(musicMapper.updateFavoriteDirectory(JwtToken.parseToken(token, UserEntity.class,secret).getId(),favoriteId,name));
    }

    /**
     * @author: wuwenqiang
     * @methodsName: insertMusicFavorite
     * @description: 插入音乐收藏，先删除原有的音乐收藏，再插入
     * @return: ResultEntity
     * @date: 2024-06-2 11:17
     */
    @Override
    public ResultEntity insertMusicFavorite(String token,Long musicId, List<MusicFavoriteEntity> myMusicFavoriteEntityList) {
        String uid = JwtToken.parseToken(token, UserEntity.class,secret).getId();
        Long deleteLength = musicMapper.deleteMusicFavorite(uid, musicId);
        if(myMusicFavoriteEntityList.size() == 0){
            return ResultUtil.success(deleteLength);
        }else{
            myMusicFavoriteEntityList.forEach(item->{
                item.setUserId(uid);
                item.setMusicId(musicId);
            });
            return ResultUtil.success(musicMapper.insertMusicFavorite(myMusicFavoriteEntityList));
        }
    }

    /**
     * @author: wuwenqiang
     * @methodsName: insertMusicFavorite
     * @description: 查询音乐是否收藏
     * @return: ResultEntity
     * @date: 2024-06-2 11:17
     */
    @Override
    public ResultEntity isMusicFavorite(String token,Long musicId) {
        return ResultUtil.success(musicMapper.isMusicFavorite(JwtToken.parseToken(token, UserEntity.class,secret).getId(),musicId));
    }
}
