package com.player.prompt.controller;

import com.player.common.entity.ResultEntity;
import com.player.prompt.entity.PromptEntity;
import com.player.prompt.service.IPromptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/service/prompt")
@RestController
public class PromptController {
    
    @Autowired
    private IPromptService promptService;

    @GetMapping("/getPrompt")
    public ResultEntity getPrompt(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(value = "tenantId",required = true) String tenantId,
            @RequestParam(value = "promptId",required = false) String promptId
    ){
        return promptService.getPrompt(userId,tenantId,promptId);
    }

    @DeleteMapping("/deletePrompt/{promptId}/{tenantId}")
    public ResultEntity deletePrompt(@PathVariable String promptId,
                                     @PathVariable String tenantId,
                                     @RequestHeader("X-User-Id") String userId) {
        return promptService.deletePrompt(promptId, userId, tenantId);
    }

    @PutMapping("/updatePrompt")
    public ResultEntity updatePrompt(@RequestBody PromptEntity promptEntity,
                                     @RequestHeader("X-User-Id") String userId) {
        return promptService.updatePrompt(promptEntity, userId);
    }

    @PutMapping("/insertPrompt")
    public ResultEntity insertPrompt(@RequestBody PromptEntity promptEntity,
                                     @RequestHeader("X-User-Id") String userId) {
        return promptService.insertPrompt(promptEntity, userId);
    }


    @GetMapping("/getPromptList")
    public ResultEntity getPromptList(@RequestHeader("X-User-Id") String userId,
                                      @RequestParam(value = "tenantId",required = true) String tenantId,
                                      @RequestParam(value = "keyword", required = false) String keyword,
                                      @RequestParam(value = "pageSize", required = true) int pageSize,
                                      @RequestParam(value = "pageNum", required = true) int pageNum) {
        return promptService.getPromptList(userId,tenantId,keyword,pageSize,pageNum);
    }
}