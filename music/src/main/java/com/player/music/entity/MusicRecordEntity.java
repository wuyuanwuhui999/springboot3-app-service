package com.player.music.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class MusicRecordEntity {
    @Schema(description = "主键")
    private int id;//主键

    @Schema(description = "用户id")
    private String userId;

    @Schema(description = "平台")
    private String platform;

    @Schema(description = "设备")
    private String device;

    @Schema(description = "app版本")
    private String version;

    @Schema(description = "用户id")
    private int musicId;
}
