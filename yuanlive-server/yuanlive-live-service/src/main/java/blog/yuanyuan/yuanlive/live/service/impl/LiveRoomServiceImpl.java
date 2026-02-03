package blog.yuanyuan.yuanlive.live.service.impl;

import blog.yuanyuan.yuanlive.common.exception.ApiException;
import blog.yuanyuan.yuanlive.common.result.Result;
import blog.yuanyuan.yuanlive.common.result.ResultCode;
import blog.yuanyuan.yuanlive.common.result.ResultPage;
import blog.yuanyuan.yuanlive.entity.live.entity.LiveCategory;
import blog.yuanyuan.yuanlive.entity.live.entity.LiveRoom;
import blog.yuanyuan.yuanlive.entity.user.entity.SysUser;
import blog.yuanyuan.yuanlive.feign.user.UserFeignClient;
import blog.yuanyuan.yuanlive.live.constant.MsgType;
import blog.yuanyuan.yuanlive.live.domain.dto.LiveRoomDTO;
import blog.yuanyuan.yuanlive.live.domain.dto.LiveRoomQueryDTO;
import blog.yuanyuan.yuanlive.live.domain.dto.SrsCallBackDTO;
import blog.yuanyuan.yuanlive.live.domain.vo.LiveRoomDetailVO;
import blog.yuanyuan.yuanlive.live.domain.vo.LiveRoomVO;
import blog.yuanyuan.yuanlive.live.mapper.LiveCategoryMapper;
import blog.yuanyuan.yuanlive.live.mapper.LiveRoomMapper;
import blog.yuanyuan.yuanlive.live.message.notification.LiveStartMessage;
import blog.yuanyuan.yuanlive.live.service.LiveCategoryService;
import blog.yuanyuan.yuanlive.live.service.LiveRoomService;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author frodepu
* @description 针对表【live_room(直播间表)】的数据库操作Service实现
* @createDate 2026-01-27 14:55:18
*/
@Service
@Slf4j
public class LiveRoomServiceImpl extends ServiceImpl<LiveRoomMapper, LiveRoom>
    implements LiveRoomService{

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private UserFeignClient userFeignClient;
    @Resource
    private LiveCategoryMapper liveCategoryMapper;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private LiveCategoryService categoryService;

    @Value("${RedisKey.active-rooms.key}")
    private String active;
    @Value("${RedisKey.room2client.prefix}")
    private String room2client;
    @Value("${yuanlive.chat.mq.exchange}")
    private String exchange;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createRoom(LiveRoomDTO roomDTO, Long anchorId) {
        // 检查该主播是否已有直播间
        LiveRoom existRoom = lambdaQuery()
                .eq(LiveRoom::getAnchorId, anchorId)
                .one();
        if (existRoom != null) {
            throw new ApiException("您已创建过直播间，无需重复创建");
        }

        // 验证分类是否存在
        validateCategory(roomDTO.getCategoryId());

        LiveRoom liveRoom = new LiveRoom();
        BeanUtils.copyProperties(roomDTO, liveRoom);
        liveRoom.setAnchorId(anchorId);
        liveRoom.setRoomStatus(0); // 默认未开播
        liveRoom.setViewCount(0);

        boolean saved = save(liveRoom);
        if (saved) {
            log.info("创建直播间成功 | 主播ID: {} | 房间ID: {}", anchorId, liveRoom.getId());
        }
        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRoom(LiveRoomDTO roomDTO, Long anchorId) {
        // 获取直播间
        LiveRoom liveRoom = getById(roomDTO.getId());
        if (liveRoom == null) {
            throw new ApiException("直播间不存在");
        }
        // 验证是否为主播本人
        if (!liveRoom.getAnchorId().equals(anchorId)) {
            throw new ApiException("无权修改他人的直播间");
        }
        // 正在直播中不允许修改
        if (liveRoom.getRoomStatus() == 1) {
            throw new ApiException("直播中无法修改房间信息");
        }
        // 验证分类是否存在
        if (roomDTO.getCategoryId() != null) {
            validateCategory(roomDTO.getCategoryId());
        }
        // 更新信息
        BeanUtils.copyProperties(roomDTO, liveRoom);
        liveRoom.setUpdateTime(new Date());

        boolean updated = updateById(liveRoom);
        if (updated) {
            log.info("修改直播间成功 | 房间ID: {} | 主播ID: {}", roomDTO.getId(), anchorId);
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean startLive(SrsCallBackDTO dto) {
        long roomId = Long.parseLong(dto.getStream());
        LiveRoom liveRoom = getById(roomId);
        Map<String, String> map = HttpUtil.decodeParamMap(dto.getParam(), Charset.defaultCharset());
        if (liveRoom == null) {
            throw new ApiException("直播间不存在");
        }
        // 通过token验证登录
        Object id = StpUtil.getLoginIdByToken(map.get("token"));
        if (id == null) {
            throw new ApiException(ResultCode.TOKEN_EXPIRED);
        }
        Long uid = Long.valueOf(String.valueOf(id));
        // 验证是否为主播本人
        if (!liveRoom.getAnchorId().equals(uid)) {
            throw new ApiException("无权操作他人的直播间");
        }
        // 检查状态
        if (liveRoom.getRoomStatus() == 1) {
            throw new ApiException("直播间已在直播中");
        }
        // 更新状态
        liveRoom.setRoomStatus(1);
        liveRoom.setLastStartTime(new Date());
        liveRoom.setUpdateTime(new Date());
        boolean updated = updateById(liveRoom);
        if (updated) {
            stringRedisTemplate.opsForSet().add(active, String.valueOf(roomId));
            stringRedisTemplate.opsForValue().set(room2client + roomId, dto.getClient_id());
            log.info("开始直播 | 房间ID: {} | 主播ID: {}", roomId, uid);
            // 发送消息给rabbitmq
            // 构造消息内容
            Result<SysUser> result = userFeignClient.getInfo(liveRoom.getAnchorId());
            if (result == null || !result.isSuccess()) {
                throw new ApiException("获取主播信息失败");
            }
            String categoryName = categoryService.lambdaQuery()
                    .select(LiveCategory::getName)
                    .eq(LiveCategory::getId, liveRoom.getCategoryId()).list().get(0).getName();
            LiveStartMessage message = LiveStartMessage.builder()
                    .roomId(String.valueOf(roomId))
                    .title(liveRoom.getTitle())
                    .userId(uid)
                    .anchorName(result.getData().getUsername())
                    .category(categoryName)
                    .coverImage(liveRoom.getCoverImg()).build();
            rabbitTemplate.convertAndSend(exchange, "", JSONUtil.toJsonStr(message));
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean endLive(SrsCallBackDTO dto) {
        long roomId = Long.parseLong(dto.getStream());
        String activeClient = stringRedisTemplate.opsForValue().get(room2client + roomId);
        if (activeClient != null && !activeClient.equals(dto.getClient_id())) {
            log.info("忽略陈旧的下播回调，当前房间[{}]已有新连接[{}]", roomId, dto.getClient_id());
            return false;
        }

        LiveRoom liveRoom = getById(roomId);
        if (liveRoom == null) {
            throw new ApiException("直播间不存在");
        }
        // 检查状态
        if (liveRoom.getRoomStatus() == 0) {
            throw new ApiException("直播间未在直播中");
        }
        // 更新状态
        liveRoom.setRoomStatus(0);
        liveRoom.setViewCount(0); // 重置在线人数
        liveRoom.setUpdateTime(new Date());
        boolean updated = updateById(liveRoom);
        if (updated) {
            log.info("结束直播 | 房间ID: {}", roomId);
            stringRedisTemplate.delete(room2client + roomId);
            stringRedisTemplate.opsForSet().remove(active, String.valueOf(roomId));
        }
        return updated;
    }

    @Override
    public LiveRoomDetailVO getRoomDetail(Long roomId) {
        LiveRoom liveRoom = getById(roomId);
        if (liveRoom == null) {
            throw new ApiException("直播间不存在");
        }

        LiveRoomDetailVO detailVO = new LiveRoomDetailVO();
        BeanUtils.copyProperties(liveRoom, detailVO);

        // 获取分类名称
        if (liveRoom.getCategoryId() != null) {
            LiveCategory category = liveCategoryMapper.selectById(liveRoom.getCategoryId());
            if (category != null) {
                detailVO.setCategoryName(category.getName());
            }
        }

        // 生成推拉流地址
        String pushUrl = generatePushUrl(roomId);
        String pullUrl = generatePullUrl(roomId);
        detailVO.setPushUrl(pushUrl);
        detailVO.setPullUrl(pullUrl);
        LiveRoom room = lambdaQuery().select(LiveRoom::getAnchorId).eq(LiveRoom::getId, roomId).list().get(0);
        log.info("room {}", room);
        SysUser user = userFeignClient.getInfo(room.getAnchorId()).getData();
        detailVO.setAnchorName(user.getUsername());
        detailVO.setAnchorAvatar(user.getAvatar());

        return detailVO;
    }

    @Override
    public LiveRoomVO getAnchorRoom(Long anchorId) {
        LiveRoom liveRoom = lambdaQuery()
                .eq(LiveRoom::getAnchorId, anchorId)
                .one();
        if (liveRoom == null) {
            return null;
        }

        return convertToVO(liveRoom);
    }

    @Override
    public ResultPage<LiveRoomVO> pageRooms(LiveRoomQueryDTO queryDTO) {
        Page<LiveRoom> page = lambdaQuery()
                .eq(queryDTO.getAnchorId() != null, LiveRoom::getAnchorId, queryDTO.getAnchorId())
                .eq(queryDTO.getRoomStatus() != null, LiveRoom::getRoomStatus, queryDTO.getRoomStatus())
                .eq(queryDTO.getCategoryId() != null, LiveRoom::getCategoryId, queryDTO.getCategoryId())
                .like(StrUtil.isNotBlank(queryDTO.getTitle()), LiveRoom::getTitle, queryDTO.getTitle())
                .orderByDesc(LiveRoom::getCreateTime)
                .page(new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()));

        List<LiveRoomVO> vos = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        Page<LiveRoomVO> voPage = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize(), page.getTotal());
        voPage.setRecords(vos);
        return ResultPage.of(voPage);
    }

    /**
     * 验证分类是否存在
     */
    private void validateCategory(Integer categoryId) {
        LiveCategory category = liveCategoryMapper.selectById(categoryId);
        if (category == null) {
            throw new ApiException("直播分类不存在");
        }
    }

    /**
     * 转换为VO
     */
    private LiveRoomVO convertToVO(LiveRoom liveRoom) {
        LiveRoomVO vo = new LiveRoomVO();
        BeanUtils.copyProperties(liveRoom, vo);

        // 获取分类名称
        if (liveRoom.getCategoryId() != null) {
            LiveCategory category = liveCategoryMapper.selectById(liveRoom.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }
        SysUser user = userFeignClient.getInfo(liveRoom.getAnchorId()).getData();
        vo.setAnchorName(user.getUsername());
        return vo;
    }

    /**
     * 生成推流地址
     */
    private String generatePushUrl(Long roomId) {
        // 示例: rtmp://localhost:1935/live/{roomId}
        return String.format("rtmp://localhost:1935/live/%d", roomId);
    }

    /**
     * 生成拉流地址
     */
    private String generatePullUrl(Long roomId) {
        // 示例: http://localhost:8080/live/{roomId}.flv
        return String.format("http://localhost:18080/live/%d.flv", roomId);
    }
}




