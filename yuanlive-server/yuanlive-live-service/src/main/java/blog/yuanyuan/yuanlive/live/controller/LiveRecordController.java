package blog.yuanyuan.yuanlive.live.controller;

import blog.yuanyuan.yuanlive.common.result.Result;
import blog.yuanyuan.yuanlive.entity.live.entity.VideoResource;
import blog.yuanyuan.yuanlive.entity.live.vo.UnseenVO;
import blog.yuanyuan.yuanlive.live.service.VideoResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/record")
@Tag(name = "录播接口")
public class LiveRecordController {
    @Resource
    private VideoResourceService videoResourceService;

    @Operation(summary = "根据id查询视频信息")
    @GetMapping("/getRecordById/{id}")
    public Result<List<VideoResource>> getRecordById(@PathVariable("id") Long id) {
        List<VideoResource> videos = videoResourceService.lambdaQuery()
                .eq(VideoResource::getUserId, id).list();
        return Result.success(videos);
    }

    @Operation(summary = "根据followingIds、lastReadVideoIds查询每个关注者的未观看视频数")
    @GetMapping("/getUnseenCount")
    public Result<List<UnseenVO>> getUnseenCount(@RequestParam("followingIds") List<Long> followingIds,
                                                 @RequestParam("lastReadVideoIds") List<Long> lastReadVideoIds) {
        return Result.success(videoResourceService.getUnseenCount(followingIds, lastReadVideoIds));
    }
}
