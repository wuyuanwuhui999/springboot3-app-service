package com.player.prompt.service.imp;

import com.player.common.entity.ResultEntity;
import com.player.common.entity.ResultUtil;
import com.player.prompt.entity.PromptEntity;
import com.player.prompt.mapper.PromptMapper;
import com.player.prompt.service.IPromptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PromptService implements IPromptService {
    @Autowired
    private PromptMapper promptMapper;


}
