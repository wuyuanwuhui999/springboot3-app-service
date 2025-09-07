package com.player.user.entity;

import com.player.common.entity.UserEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SearchUserEntity extends UserEntity {

    @Schema(description = "是否在租户中（0-否，1-是）")
    private Integer checked;
}
