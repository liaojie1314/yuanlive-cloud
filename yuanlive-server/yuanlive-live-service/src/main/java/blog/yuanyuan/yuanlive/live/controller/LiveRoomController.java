package blog.yuanyuan.yuanlive.live.controller;

import blog.yuanyuan.yuanlive.common.result.Result;
import blog.yuanyuan.yuanlive.common.result.ResultPage;
import blog.yuanyuan.yuanlive.live.domain.dto.LiveRoomDTO;
import blog.yuanyuan.yuanlive.live.domain.dto.LiveRoomQueryDTO;
import blog.yuanyuan.yuanlive.live.domain.dto.SrsCallBackDTO;
import blog.yuanyuan.yuanlive.live.domain.vo.LiveRoomDetailVO;
import blog.yuanyuan.yuanlive.live.domain.vo.LiveRoomVO;
import blog.yuanyuan.yuanlive.live.service.LiveRoomService;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Update;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "直播间管理")
@RequestMapping("/room")
@Slf4j
public class LiveRoomController {
    @Resource
    private LiveRoomService liveRoomService;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Value("${yuanlive.chat.mq.exchange}")
    private String exchange;

    @PostMapping("/create")
    @Operation(summary = "创建直播间")
    @SaCheckRole("ANCHOR")
    public Result<String> createRoom(@RequestBody @Validated LiveRoomDTO roomDTO) {
        Long anchorId = StpUtil.getLoginIdAsLong();
        if (liveRoomService.createRoom(roomDTO, anchorId)) {
            return Result.success(null, "创建直播间成功");
        }
        return Result.failed("创建直播间失败");
    }

    @PostMapping("/update")
    @Operation(summary = "修改直播间信息")
    @SaCheckRole("ANCHOR")
    public Result<String> updateRoom(@RequestBody @Validated(Update.class) LiveRoomDTO roomDTO) {
        Long anchorId = StpUtil.getLoginIdAsLong();
        if (liveRoomService.updateRoom(roomDTO, anchorId)) {
            return Result.success(null, "修改成功");
        }
        return Result.failed("修改失败");
    }

    @PostMapping("/start")
    @Operation(summary = "开始直播")
    public Result<String> startLive(@RequestBody SrsCallBackDTO srsCallBackDTO) {
        if (liveRoomService.startLive(srsCallBackDTO)) {
            return Result.success(null, "开始直播成功");
        }
        return Result.failed("开始直播失败");
    }

    @PostMapping("/end")
    @Operation(summary = "结束直播")
    public Result<String> endLive(@RequestBody SrsCallBackDTO dto) {
        if (liveRoomService.endLive(dto)) {
            return Result.success(null, "结束直播成功");
        }
        return Result.failed("结束直播失败");
    }

    @GetMapping("/detail/{roomId}")
    @Operation(summary = "获取直播间详情")
    public Result<LiveRoomDetailVO> getRoomDetail(@PathVariable("roomId") Long roomId) {
        LiveRoomDetailVO detail = liveRoomService.getRoomDetail(roomId);
        return Result.success(detail);
    }

    @GetMapping("/myRoom")
    @Operation(summary = "获取当前主播的直播间")
    @SaCheckRole("ANCHOR")
    public Result<LiveRoomVO> getMyRoom() {
        Long anchorId = StpUtil.getLoginIdAsLong();
        LiveRoomVO room = liveRoomService.getAnchorRoom(anchorId);
        return Result.success(room);
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询直播间列表")
    public Result<ResultPage<LiveRoomVO>> list(@ParameterObject LiveRoomQueryDTO queryDTO) {
        ResultPage<LiveRoomVO> page = liveRoomService.pageRooms(queryDTO);
        return Result.success(page);
    }

    @GetMapping("/anchor/{anchorId}")
    @Operation(summary = "根据主播ID获取直播间")
    public Result<LiveRoomVO> getAnchorRoom(@PathVariable("anchorId") Long anchorId) {
        LiveRoomVO room = liveRoomService.getAnchorRoom(anchorId);
        return Result.success(room);
    }
}
