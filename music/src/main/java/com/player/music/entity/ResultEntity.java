package com.player.music.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ResultEntity {

    @Schema(description = "数据")
    private Object data;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "信息")
    private String msg;
}
