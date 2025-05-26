package com.player.ai.mapper;

import com.player.ai.entity.MusicEntity;

import java.util.List;

public interface MusicMapper {
    List<MusicEntity> selectMusicList(String name);

}
