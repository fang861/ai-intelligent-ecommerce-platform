package com.scaffold.controller;

import com.scaffold.agent.MallAgentService;
import com.scaffold.common.AuthContext;
import com.scaffold.common.Result;
import com.scaffold.dto.AiChatRequest;
import com.scaffold.dto.AiGuideResponse;
import com.scaffold.service.AiService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Autowired
    private AiService aiService;
    @Autowired
    private MallAgentService mallAgentService;

    @PostMapping("/guide")
    public Result<AiGuideResponse> guide(@RequestBody AiChatRequest request) {
        return Result.ok(aiService.guide(request.getMessage()));
    }

    /**
     * Spring AI Agent：导购 + RAG + 购物车/订单 Tool Calling。
     * 可选登录：未登录仅导购与答疑；加购/下单需 JWT。
     */
    @PostMapping("/agent")
    public Result<AiGuideResponse> agent(HttpServletRequest request, @RequestBody AiChatRequest body) {
        Long userId = AuthContext.getUserId(request);
        return Result.ok(mallAgentService.chat(body.getMessage(), userId, body.getHistory()));
    }

    /** 上传图片识图导购（传已上传后的 imageUrl，可附带补充说明） */
    @PostMapping("/guide/vision")
    public Result<AiGuideResponse> guideByImage(@RequestBody AiChatRequest request) {
        return Result.ok(aiService.guideByImage(request.getImageUrl(), request.getMessage()));
    }

    @PostMapping("/service")
    public Result<Map<String, String>> customerService(HttpServletRequest request,
                                                       @RequestBody AiChatRequest body) {
        Long userId = AuthContext.requireUser(request);
        String reply = aiService.customerService(body.getMessage(), userId);
        return Result.ok(Map.of("reply", reply));
    }
}
