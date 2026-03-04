package blog.yuanyuan.yuanlive.feign.live;

import blog.yuanyuan.yuanlive.common.result.Result;
import blog.yuanyuan.yuanlive.common.result.ResultPage;
import blog.yuanyuan.yuanlive.entity.live.dto.SearchQueryDTO;
import blog.yuanyuan.yuanlive.entity.live.vo.LiveRoomRankVO;
import blog.yuanyuan.yuanlive.entity.live.vo.LiveRoomVO;
import blog.yuanyuan.yuanlive.entity.live.vo.SearchVO;
import blog.yuanyuan.yuanlive.entity.live.vo.UnseenVO;
import io.swagger.v3.oas.annotations.Operation;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(value = "live-service")
public interface LiveFeignClient {
    @GetMapping("/room/getRoom/{uid}")
    public Result<LiveRoomVO> getRoomByUid(@PathVariable("uid") Long uid);

    @GetMapping("/record/getUnseenCount")
    public Result<List<UnseenVO>> getUnseenCount(@RequestParam("followingIds") List<Long> followingIds,
                                                 @RequestParam("lastReadVideoIds") List<Long> lastReadVideoIds);

    @PostMapping("/room/search")
    public ResultPage<SearchVO> search(@RequestBody @Validated SearchQueryDTO searchQueryDTO);
}
