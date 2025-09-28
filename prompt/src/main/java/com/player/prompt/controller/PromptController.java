package com.player.prompt.controller;

import com.player.common.entity.ResultEntity;
import com.player.common.utils.JwtToken;
import com.player.prompt.service.IPromptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/service/tenant")
@RestController
public class PromptController {
    @Value("${token.secret}")
    private String secret;

    @Autowired
    private IPromptService promptService;

}
