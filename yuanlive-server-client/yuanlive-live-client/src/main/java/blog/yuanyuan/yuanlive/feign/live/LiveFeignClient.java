package blog.yuanyuan.yuanlive.feign.live;

import blog.yuanyuan.yuanlive.common.result.Result;
import blog.yuanyuan.yuanlive.entity.live.vo.LiveRoomVO;
import blog.yuanyuan.yuanlive.entity.live.vo.UnseenVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "live-service")
public interface LiveFeignClient {
    @GetMapping("/room/getRoom/{uid}")
    public Result<LiveRoomVO> getRoomByUid(@PathVariable("uid") Long uid);

    @GetMapping("/record/getUnseenCount")
    public Result<List<UnseenVO>> getUnseenCount(@RequestParam("followingIds") List<Long> followingIds,
                                                 @RequestParam("lastReadVideoIds") List<Long> lastReadVideoIds);
}
