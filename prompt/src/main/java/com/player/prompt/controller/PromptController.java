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
    @Value("${token.secret}")
    private String secret;

    @Autowired
    private IPromptService promptService;

    @PostMapping("/insertPrompt")
    public ResultEntity insertPrompt(@RequestBody UserPromptEntity userPromptEntity,
                                     @RequestHeader(value = "Authorization", required = false) String token) {
        return promptService.insertPrompt(userPromptEntity, JwtToken.getId(token,secret));
    }

    @DeleteMapping("/deletePrompt/{tenantId}/{id}")
    public ResultEntity deletePrompt(@PathVariable String id,
                                     @PathVariable String tenantId,
                                     @RequestHeader(value = "Authorization", required = false) String token) {
        return promptService.deletePrompt(id, JwtToken.getId(token,secret),tenantId);
    }

    @PutMapping("/updatePrompt")
    public ResultEntity updatePrompt(@RequestBody UserPromptEntity userPromptEntity,
                                     @RequestHeader(value = "Authorization", required = false) String token) {
        return promptService.updatePrompt(userPromptEntity, JwtToken.getId(token,secret));
    }

    @GetMapping("/getPromptById/{tenantId}/{id}")
    public ResultEntity getPromptById(@PathVariable String id,
                                      @PathVariable String tenantId,
                                      @RequestHeader(value = "Authorization", required = false) String token) {
        return promptService.getPromptById(id, JwtToken.getId(token,secret),tenantId);
    }

    @GetMapping("/getPromptList")
    public ResultEntity getPromptList(@RequestHeader(value = "Authorization", required = false) String token,
                                      @RequestParam(value = "tenantId",required = true) String tenantId,
                                      @RequestParam(value = "content", required = false) String content,
                                      @RequestParam(value = "industry", required = false) String industry,
                                      @RequestParam(value = "tags", required = false) String tags) {
        return promptService.getPromptList(JwtToken.getId(token,secret),tenantId, content, industry, tags);
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
            @RequestHeader(value = "Authorization", required = false)String token
            ) {
        return promptService.getSystemPromptListByCategory(categoryId,keyword,JwtToken.getId(token,secret),pageNum,pageSize);
    }


}