package com.player.gateway.chat.mapper;

import com.player.gateway.chat.entity.MusicEntity;

import java.util.List;

public interface MusicMapper {
    List<MusicEntity> selectMusicListByAuthor(String name);

}
