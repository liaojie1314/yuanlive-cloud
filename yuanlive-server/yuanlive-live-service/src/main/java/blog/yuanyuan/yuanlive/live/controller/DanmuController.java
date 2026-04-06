package blog.yuanyuan.yuanlive.live.controller;

import blog.yuanyuan.yuanlive.common.result.Result;
import blog.yuanyuan.yuanlive.entity.live.entity.Danmu;
import blog.yuanyuan.yuanlive.live.service.IDanmuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/danmu")
@Tag(name = "视频弹幕接口")
public class DanmuController {

    @Resource
    private IDanmuService danmuService;

    @PostMapping("/send")
    @Operation(summary = "发送弹幕")
    public Result<String> sendDanmu(@RequestBody Danmu danmu) {
        if (danmuService.sendDanmu(danmu)) {
            return Result.success(null, "发送弹幕成功");
        }
        return Result.failed("发送弹幕失败");
    }
}