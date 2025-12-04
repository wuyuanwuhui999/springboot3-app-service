package com.player.gateway.movie.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
public class MovieEntity {
    @Schema(description = "主键")
    private Long id;//主键

    @Schema(description = "电影id")
    private Long movieId;

    @Schema(description = "导演")
    private String director;//导演

    @Schema(description = "主演")
    private String star;//主演

    @Schema(description = "类型")
    private String type;//类型

    @Schema(description = "国家/语言")
    private String countryLanguage;//国家/语言

    @Schema(description = "观看状态")
    private String viewingState;//观看状态

    @Schema(description = "上映时间")
    private String releaseTime;//上映时间

    @Schema(description = "剧情")
    private String plot;//剧情

    @Schema(description = "更新时间")
    private Date updateTime;//更新时间

    @Schema(description = "电影名称")
    private String movieName;//电影名称

    @Schema(description = "是否推荐，0:不推荐，1:推荐")
    private String isRecommend;//是否推荐，0:不推荐，1:推荐

    @Schema(description = "电影海报")
    private String img;//电影海报

    @Schema(description = "分类 电影,电视剧,动漫,综艺,新片库,福利,午夜,恐怖,其他")
    private String classify;//分类 电影,电视剧,动漫,综艺,新片库,福利,午夜,恐怖,其他

    @Schema(description = "来源名称，本地，骑士影院，爱奇艺")
    private String sourceName;//来源名称，本地，骑士影院，爱奇艺

    @Schema(description = "来源地址")
    private String sourceUrl;//来源地址

    @Schema(description = "创建时间")
    private Date createTime;//创建时间

    @Schema(description = "本地图片")
    private String localImg;//本地图片

    @Schema(description = "播放集数")
    private String label;//播放集数

    @Schema(description = "源地址")
    private String originalHref;//源地址

    @Schema(description = "简单描述")
    private String description;//简单描述

    @Schema(description = "链接地址")
    private String targetHref;//链接地址

    @Schema(description = "0代表未使用，1表示正在使用，是banner和carousel图的才有")
    private String useStatus;//0代表未使用，1表示正在使用，是banner和carousel图的才有

    @Schema(description = "评分")
    private Double score;//评分

    @Schema(description = "类目，值为banner首屏，carousel：滚动轮播")
    private String category;//类目，值为banner首屏，carousel：滚动轮播

    @Schema(description = "排名")
    private String ranks;//排名

    @Schema(description = "用户名，这这个表不需要，为了跟记录叫和收藏表的结构一致',")
    private String userId;//用户名，这这个表不需要，为了跟记录叫和收藏表的结构一致',

    @Schema(description = "豆瓣网的url',")
    private String doubanUrl;

    @Schema(description = "播放时长")
    private int duration;

    @Schema(description = "观看权限")
    private int privilegeId;
}
