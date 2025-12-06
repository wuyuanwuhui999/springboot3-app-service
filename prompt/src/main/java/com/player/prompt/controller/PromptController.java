package com.player.prompt.controller;

import com.player.common.entity.ResultEntity;
import com.player.common.utils.JwtToken;
import com.player.prompt.entity.UserPromptEntity;
import com.player.prompt.service.IPromptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/service/prompt")
@RestController
public class PromptController {
    
    @Autowired
    private IPromptService promptService;

    @PostMapping("/insertPrompt")
    public ResultEntity insertPrompt(@RequestBody UserPromptEntity userPromptEntity,
                                     @RequestHeader("X-User-Id") String userId) {
        return promptService.insertPrompt(userPromptEntity, userId);
    }

    @DeleteMapping("/deletePrompt/{tenantId}/{id}")
    public ResultEntity deletePrompt(@PathVariable String id,
                                     @PathVariable String tenantId,
                                     @RequestHeader("X-User-Id") String userId) {
        return promptService.deletePrompt(id, userId,tenantId);
    }

    @PutMapping("/updatePrompt")
    public ResultEntity updatePrompt(@RequestBody UserPromptEntity userPromptEntity,
                                     @RequestHeader("X-User-Id") String userId) {
        return promptService.updatePrompt(userPromptEntity, userId);
    }

    @GetMapping("/getPromptById/{tenantId}/{id}")
    public ResultEntity getPromptById(@PathVariable String id,
                                      @PathVariable String tenantId,
                                      @RequestHeader("X-User-Id") String userId) {
        return promptService.getPromptById(id, userId,tenantId);
    }

    @GetMapping("/getPromptList")
    public ResultEntity getPromptList(@RequestHeader("X-User-Id") String userId,
                                      @RequestParam(value = "tenantId",required = true) String tenantId,
                                      @RequestParam(value = "content", required = false) String content,
                                      @RequestParam(value = "industry", required = false) String industry,
                                      @RequestParam(value = "tags", required = false) String tags) {
        return promptService.getPromptList(userId,tenantId, content, industry, tags);
    }

    @GetMapping("/getPromptCategoryList")
    public ResultEntity getPromptCategoryList() {
        return promptService.getPromptCategoryList();
    }

    @GetMapping("/getSystemPromptListByCategory")
    public ResultEntity getSystemPromptListByCategory(
            @RequestParam(value = "categoryId") String categoryId,
            @RequestParam(value = "pageNum") int pageNum,
            @RequestParam(value = "pageSize") int pageSize,
            @RequestParam(value = "keyword",required = false)String  keyword,
            @RequestHeader("X-User-Id") String userId
            ) {
        return promptService.getSystemPromptListByCategory(categoryId,keyword,userId,pageNum,pageSize);
    }

    @PostMapping("/insertCollectPrompt/{tenantId}/{promptId}")
    public ResultEntity insertCollectPrompt(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("promptId") String promptId,
            @RequestHeader("X-User-Id") String userId
    )
    {
        return promptService.insertCollectPrompt(tenantId,promptId,userId);
    }

    @DeleteMapping("/deleteCollectPrompt/{tenantId}/{promptId}")
    public ResultEntity deleteCollectPrompt(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("promptId") String promptId,
            @RequestHeader("X-User-Id") String userId
    )
    {
        return promptService.deleteCollectPrompt(tenantId,promptId,userId);
    }

    @GetMapping("/getMyCollectPromptCategory")
    public ResultEntity getMyCollectPromptCategory(
            @RequestParam("tenantId") String tenantId,
            @RequestHeader("X-User-Id") String userId
    )
    {
        return promptService.getMyCollectPromptCategory(tenantId,userId);
    }

    @GetMapping("/getMyCollectPromptList")
    public ResultEntity getMyCollectPromptList(
            @RequestParam(value = "tenantId") String tenantId,
            @RequestParam(value = "categoryId", required = false) String categoryId,
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(value = "pageNum")int pageNum,
            @RequestParam(value = "pageSize")int pageSize
    )
    {
        return promptService.getMyCollectPromptList(tenantId,categoryId,userId,pageNum,pageSize);
    }
}