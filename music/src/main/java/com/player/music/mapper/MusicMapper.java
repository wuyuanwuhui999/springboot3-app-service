package com.player.music.mapper;

import com.player.common.entity.LogEntity;
import com.player.music.entity.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface MusicMapper {
    MusicEntity getKeywordMusic();

    List<MusicCategoryEntity> getMusicCategory();

    List<MusicEntity> getMusicListByCategoryId(int categoryId,int start,int pageSize,String userId);

    Long getMusicTotalByCategoryId(int categoryId);

    List<MusicAuthorEntity> getMusicAuthorListByCategoryId(String userId, int categoryId, int start, int pageSize);

    Long getMusicAuthorTotal(int categoryId);

    List<MusicEntity> getMusicListByAuthorId(String userId,String authorId,String authorName, int start, int pageSize);

    Long getMusicListByAuthorTotal(String authorId,String authorName);

    List<MusicAuthorEntity> getFavoriteAuthor(String userId, int start, int pageSize);

    Long getFavoriteAuthorCount(String userId);

    Long insertFavoriteAuthor(String userId,int authorId);

    Long deleteFavoriteAuthor(String userId,int authorId);

    List<MusicEntity> getMusicRecord(String userId, Date startDate,Date endDate, int start, int pageSize);

    Long getMusicRecordCount(String userId);

    Long insertMusicRecord(MusicRecordEntity myMusicRecordEntity);

    /**
     * 用户播放记录统计表自增（user_id,music_id 唯一，播放一次 +1，同时刷新 last_play_time / update_time）
     */
    Long incrementUserMusicPlayStat(String userId, int musicId, Date playTime);

    /**
     * 歌曲播放记录统计表自增（music_id 唯一，播放一次 +1，同时刷新 last_play_time / update_time）
     */
    Long incrementMusicPlayStat(int musicId, Date playTime);

    Long insertMusicLike(String userId,int musicId);

    Long deleteMusicLike(String userId,int musicId);

    List<MusicEntity> getMusicLike(String userId, int start, int pageSize);

    Long getMusicLikeCount(String userId);

    List<MusicEntity> searchMusic(String userId,String keyword, int start, int pageSize);

    Long searchMusicCount(String keyword);

    List<MusicAuthorCategoryEntity> getMusicAuthorCategory();

    List<MusicFavoriteDirectoryEntity> getFavoriteDirectory(String userId, Long musicId);

    List<MusicEntity> getMusicListByFavoriteId(String userId,Long favoriteId,int start,int pageSize);

    Long getMusicCountByFavoriteId(Long favoriteId);

    Long deleteFavoriteDirectory(String userId, Long favoriteId);

    Long updateFavoriteDirectory(String userId, Long favoriteId, String name);

    Long insertFavoriteDirectory(MusicFavoriteDirectoryEntity favoriteDirectoryEntity);

    MusicFavoriteDirectoryEntity getFavoriteDirectoryById(Long id);

    Long insertMusicFavorite(List<MusicFavoriteEntity> myMusicFavoriteEntityList);

    Long deleteMusicFavorite(String userId,Long musicId);

    Long deleteMusicFavoriteByFavoriteId(String userId,Long favoriteId);

    Long isMusicFavorite(String userId,Long musicId);

    List<MusicEntity> queryMusic(String songName, String authorName, String albumName, String language, Date publishStart, String label, int pageNum, int pageSize);

    Long queryMusicCount(String songName, String authorName, String albumName, String language, Date publishStart, String label);

    MusicEntity getMusicById(int musicId);

    List<MusicEntity> getRecommendMusicByLabels(@Param("musicId") int musicId, @Param("labels") List<String> labels, @Param("userId") String userId, @Param("start") int start, @Param("pageSize") int pageSize);

    List<MusicEntity> getRecommendMusicByAuthorId(@Param("authorId") String authorId, @Param("excludeMusicId") int excludeMusicId, @Param("userId") String userId, @Param("start") int start, @Param("pageSize") int pageSize);
}
