package com.player.chat.mapper;

import com.player.chat.entity.MusicEntity;

import java.util.List;

public interface MusicMapper {
    List<MusicEntity> selectMusicListByAuthor(String name);

}
