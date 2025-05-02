package com.player.music.entity;

//import com.baomidou.mybatisplus.annotation.IdType;
//import com.baomidou.mybatisplus.annotation.TableId;
//import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
public class MusicEntity {

    @Schema(description = "主键")
    private Long id;//主键

    @Schema(description = "专辑id")
    private Long albumId;

    @Schema(description = "歌曲名称")
    private String songName;

    @Schema(description = "歌手名称")
    private String authorName;

    @Schema(description = "歌手id")
    private Long authorId;

    @Schema(description = "专辑")
    private String albumName;

    @Schema(description = "版本")
    private String version;

    @Schema(description = "语言")
    private String language;

    @Schema(description = "发布时间")
    private Date publishDate;

    @Schema(description = "未使用字段")
    private Long wideAudioId;

    @Schema(description = "是否发布")
    private Long isPublish;

    @Schema(description = "未使用字段")
    private Long bigPackId;

    @Schema(description = "未使用字段")
    private Long finalId;

    @Schema(description = "音频id")
    private Long audioId;

    @Schema(description = "未使用字段")
    private Long similarAudioId;

    @Schema(description = "是否热门")
    private int isHot;

    @Schema(description = "音频专辑id")
    private Long albumAudioId;

    @Schema(description = "歌曲组id")
    private Long audioGroupId;

    @Schema(description = "歌曲图片")
    private String cover;

    @Schema(description = "网络播放地址")
    private String playUrl;

    @Schema(description = "本地播放地址")
    private String localPlayUrl;

    @Schema(description = "歌曲来源")
    private String sourceName;

    @Schema(description = "来源地址")
    private String sourceUrl;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;

    @Schema(description = "标签")
    private String label;

    @Schema(description = "歌词")
    private String lyrics;

    @Schema(description = "播放权限")
    private int permission;

    @Schema(description = "是否是喜欢，0表示不在喜欢的列表中，1表示在喜欢的列表中")
    private int isLike;

    @Schema(description = "听过的次数，在获取播放记录的时候才有")
    private int times;
}
