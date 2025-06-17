package com.player.music.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class FileEntity {
    @Schema(description = "base64字符串")
    private String[] base64;
}