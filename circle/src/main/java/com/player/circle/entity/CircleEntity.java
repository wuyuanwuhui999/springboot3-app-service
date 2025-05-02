package com.player.circle.entity;

import com.player.common.entity.CommentEntity;
import com.player.common.entity.LikeEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class CircleEntity {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "关联音乐audio_id或者电影movie_id")
    private Long relationId;

    @Schema(description = "朋友圈内容")
    private String content;

    @Schema(description = "朋友圈图片")
    private String imgs;

    @Schema(description = "类型")
    private String type;

    @Schema(description = "用户id")
    private String userId;

    @Schema(description = "用户的昵称")
    private String username;

    @Schema(description = "用户头像")
    private String useravater;

    @Schema(description = "权限，0不公开，1公开")
    private int permission;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;

    @Schema(description = "歌曲名称")
    private String musicSongName;

    @Schema(description = "歌曲id")
    private String musicAudioId;

    @Schema(description = "歌曲作者")
    private String musicAuthorName;

    @Schema(description = "专辑名称")
    private String musicAlbumName;

    @Schema(description = "音乐图片")
    private String musicCover;

    @Schema(description = "音乐播放地址")
    private String musicPlayUrl;

    @Schema(description = "音乐本地播放地址")
    private String musicLocalPlayUrl;

    @Schema(description = "歌词")
    private String musicLyrics;

    @Schema(description = "电影id")
    private String movieId;

    @Schema(description = "电影名称")
    private String movieName;

    @Schema(description = "电影导演")
    private String movieDirector;

    @Schema(description = "电影主演")
    private String movieStar;

    @Schema(description = "电影类型")
    private String movieType;

    @Schema(description = "电影上映国家")
    private String movieCountryLanguage;

    @Schema(description = "电影状态")
    private String movieViewingState;

    @Schema(description = "上映时间")
    private String movieReleaseTime;

    @Schema(description = "电影海报")
    private String movieImg;

    @Schema(description = "电影分类")
    private String movieClassify;

    @Schema(description = "电影本地图片")
    private String movieLocalImg;

    @Schema(description = "电影得分")
    private String movieScore;

    @Schema(description = "喜欢列表")
    private List<LikeEntity> circleLikes;

    @Schema(description = "喜欢列表")
    private List<CommentEntity> circleComments;
}
