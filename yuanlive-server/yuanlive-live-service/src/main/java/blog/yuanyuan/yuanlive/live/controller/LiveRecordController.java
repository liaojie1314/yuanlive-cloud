package blog.yuanyuan.yuanlive.live.controller;

import blog.yuanyuan.yuanlive.common.result.Result;
import blog.yuanyuan.yuanlive.entity.live.entity.LiveRecord;
import blog.yuanyuan.yuanlive.live.service.LiveRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "直播记录相关接口")
@Slf4j
@RequestMapping("/record")
public class LiveRecordController {
    @Resource
    private LiveRecordService liveRecordService;

    @Operation(summary = "根据主播id获取直播记录")
    @GetMapping("/list/{anchorId}")
    public Result<List<LiveRecord>> getRecordById(@PathVariable("anchorId") Long anchorId) {
        List<LiveRecord> result = liveRecordService.lambdaQuery()
                .eq(LiveRecord::getAnchorId, anchorId).list();
        return Result.success(result);
    }
}
