package blog.yuanyuan.yuanlive.ai.controller;

import blog.yuanyuan.yuanlive.ai.domain.dto.HistoryRequestDTO;
import blog.yuanyuan.yuanlive.ai.domain.dto.SessionDeleteDTO;
import blog.yuanyuan.yuanlive.ai.domain.dto.SessionPageQueryDTO;
import blog.yuanyuan.yuanlive.ai.domain.dto.TitleUpdateDTO;
import blog.yuanyuan.yuanlive.ai.domain.vo.AiSessionVO;
import blog.yuanyuan.yuanlive.ai.domain.vo.ChatHistoryResponseVO;
import blog.yuanyuan.yuanlive.ai.domain.vo.PinSessionVO;
import blog.yuanyuan.yuanlive.ai.service.AiHistoryService;
import blog.yuanyuan.yuanlive.common.result.Result;
import blog.yuanyuan.yuanlive.common.result.ResultPage;
import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/history")
@Tag(name = "AI历史记录管理")
public class AiHistoryController {
    @Resource
    private AiHistoryService aiHistoryService;

    @GetMapping("/listSessions")
    @Operation(summary = "获取用户AI历史会话列表")
    public Result<ResultPage<AiSessionVO>> getUserSessionList(@ParameterObject SessionPageQueryDTO queryDTO) {
        return Result.success(aiHistoryService.getUserSessionList(queryDTO));
    }

    @PostMapping("/updateTitle")
    @Operation(summary = "修改AI会话名称title")
    public Result<String> updateTitle(@RequestBody @Validated TitleUpdateDTO titleUpdateDTO) {
        return aiHistoryService.updateTitle(titleUpdateDTO);
    }

    @Operation(summary = "置顶AI会话")
    @GetMapping("/pinSession/{conversationId}")
    public Result<PinSessionVO> pinSession(@PathVariable("conversationId") String conversationId) {
        return aiHistoryService.pinSession(conversationId);
    }

    @Operation(summary = "取消置顶AI会话")
    @GetMapping("/unpinSession/{conversationId}")
    public Result<PinSessionVO> unpinSession(@PathVariable("conversationId") String conversationId) {
        return aiHistoryService.unpinSession(conversationId);
    }

    @Operation(summary = "批量删除AI会话")
    @DeleteMapping("/deleteSessions")
    public Result<String> deleteSessions(@RequestBody @Validated SessionDeleteDTO deleteDTO) {
        return aiHistoryService.deleteSessions(deleteDTO);
    }

    @Operation(summary = "删除所有AI历史会话")
    @DeleteMapping("/deleteAllSessions")
    public Result<String> deleteAllSessions() {
        return aiHistoryService.deleteAllSessions();
    }

    @PostMapping("/messages/{conversationId}")
    @Operation(summary = "获取会话消息列表")
    public Result<ChatHistoryResponseVO> getMessages(@PathVariable("conversationId") String conversationId,
                                                      @RequestBody @Validated HistoryRequestDTO historyRequestDTO) {
        return Result.success(aiHistoryService.getChatHistory(conversationId, historyRequestDTO));
    }

    @GetMapping("/getRecommendations")
    @Operation(summary = "推荐三条AI搜索提问")
    public Result<List<String>> getRecommendations() {
        return aiHistoryService.getRecommendations();
    }
}
