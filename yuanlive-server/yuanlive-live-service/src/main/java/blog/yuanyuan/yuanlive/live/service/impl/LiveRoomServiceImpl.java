package blog.yuanyuan.yuanlive.live.service.impl;

import blog.yuanyuan.yuanlive.common.exception.ApiException;
import blog.yuanyuan.yuanlive.common.result.Result;
import blog.yuanyuan.yuanlive.common.result.ResultCode;
import blog.yuanyuan.yuanlive.common.result.ResultPage;
import blog.yuanyuan.yuanlive.common.util.MinioTemplate;
import blog.yuanyuan.yuanlive.entity.live.entity.LiveCategory;
import blog.yuanyuan.yuanlive.entity.live.entity.LiveRoom;
import blog.yuanyuan.yuanlive.entity.live.entity.VideoResource;
import blog.yuanyuan.yuanlive.entity.user.entity.SysUser;
import blog.yuanyuan.yuanlive.feign.user.UserFeignClient;
import blog.yuanyuan.yuanlive.live.domain.dto.LiveRoomDTO;
import blog.yuanyuan.yuanlive.live.domain.dto.LiveRoomQueryDTO;
import blog.yuanyuan.yuanlive.live.domain.dto.SrsCallBackDTO;
import blog.yuanyuan.yuanlive.live.domain.vo.LiveRoomDetailVO;
import blog.yuanyuan.yuanlive.entity.live.vo.LiveRoomVO;
import blog.yuanyuan.yuanlive.live.domain.vo.LiveRoomRankVO;
import blog.yuanyuan.yuanlive.live.mapper.LiveCategoryMapper;
import blog.yuanyuan.yuanlive.live.mapper.LiveRoomMapper;
import blog.yuanyuan.yuanlive.live.message.notification.LiveStartMessage;
import blog.yuanyuan.yuanlive.live.properties.LiveRoomProperties;
import blog.yuanyuan.yuanlive.live.service.LiveCategoryService;
import blog.yuanyuan.yuanlive.live.service.LiveRoomService;
import blog.yuanyuan.yuanlive.live.service.VideoResourceService;
import blog.yuanyuan.yuanlive.live.util.PopularityUtil;
import blog.yuanyuan.yuanlive.live.util.VideoProcessResult;
import blog.yuanyuan.yuanlive.live.util.VideoProcessUtil;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import me.ahoo.cosid.provider.IdGeneratorProvider;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ws.schild.jave.EncoderException;

import java.io.File;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
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
    private IdGeneratorProvider idGeneratorProvider;
    @Resource
    private UserFeignClient userFeignClient;
    @Resource
    private LiveCategoryMapper liveCategoryMapper;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private LiveCategoryService categoryService;
    @Resource
    private VideoResourceService videoResourceService;
    @Resource
    private MinioTemplate minioTemplate;
    @Resource
    private VideoProcessUtil videoProcessUtil;

    @Value("${redis-key.active-rooms.key}")
    private String active;
    @Value("${redis-key.anchor-map.key}")
    private String anchorMap;
    @Value("${redis-key.room2client.prefix}")
    private String room2client;
    @Value("${live.mq.chat.exchange}")
    private String exchange;
    @Value("${live.mq.stats.exchange}")
    private String statsExchange;
    @Value("${file-prefix.host-prefix}")
    private String filePrefix;
    @Value("${live.mq.stats.routing-key.video}")
    private String videoRoutingKey;
    @Resource
    private LiveRoomProperties liveRoomProperties;
    @Resource
    private PopularityUtil popularityUtil;

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
        liveRoom.setId(idGeneratorProvider.getRequired("safe-js").generate());

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
            Result<SysUser> result = userFeignClient.getInfo(liveRoom.getAnchorId());
            if (result == null || !result.isSuccess()) {
                throw new ApiException("获取主播信息失败");
            }
            VideoResource video = new VideoResource();
            video.setId(idGeneratorProvider.getRequired("video").generate());
            video.setUserId(liveRoom.getAnchorId());
            video.setRoomId(roomId);
            video.setType(0);
            video.setStartTime(new Date());
            // 直播标题(主播名-直播间title-日期-直播回放)
            String format = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy年MM月dd日HH:mm"));
            String title = result.getData().getUsername() + "-"
                    + liveRoom.getTitle() + "-" + format + "-直播回放";
            video.setTitle(title);
            videoResourceService.save(video);
            // redis存储会话信息
            String sessionKey = liveRoomProperties.getSessionPrefix() + roomId;
            Map<String, String> session = new HashMap<>();
            session.put("recordId", String.valueOf(video.getId()));
            session.put("peak", "0");
            session.put("client", dto.getClient_id());
            session.put("anchor", result.getData().getUsername());
            session.put("anchorId", liveRoom.getAnchorId().toString());
            session.put("roomTitle", liveRoom.getTitle());
            session.put("categoryId", liveRoom.getCategoryId().toString());
            session.put("coverImg", liveRoom.getCoverImg());
            stringRedisTemplate.opsForHash().putAll(sessionKey, session);
            stringRedisTemplate.persist(sessionKey);
            // redis存储每个类别的房间
            String categoryKey = liveRoomProperties.getCategoryRoomsPrefix() + liveRoom.getCategoryId();
            stringRedisTemplate.opsForSet().add(categoryKey, String.valueOf(roomId));
            // redis存储人气排行榜
            String rankingKey = liveRoomProperties.getMainRank();
            stringRedisTemplate.opsForZSet().add(rankingKey, String.valueOf(roomId), 0.0);
            // redis存储当前直播的房间ID
            stringRedisTemplate.opsForSet().add(active, String.valueOf(roomId));
            stringRedisTemplate.opsForHash().put(anchorMap, String.valueOf(uid), String.valueOf(roomId));
            log.info("开始直播 | 房间ID: {} | 主播ID: {}", roomId, uid);
            // 发送消息给rabbitmq
            // 构造消息内容
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
        log.info("结束直播 | 房间ID: {}", dto.getStream());
        long roomId = Long.parseLong(dto.getStream());
        String sessionKey = liveRoomProperties.getSessionPrefix() + roomId;
        Map<Object, Object> session = stringRedisTemplate.opsForHash().entries(sessionKey);
        log.info("结束直播 | 房间ID: {} | 会话信息: {}", roomId, session);
        String activeClient = (String) session.get("client");
        if (activeClient != null && !activeClient.equals(dto.getClient_id())) {
            log.info("忽略陈旧的下播回调，当前房间[{}]已有新连接[{}]", roomId, dto.getClient_id());
            return false;
        }
        Long recordId = Long.valueOf(session.get("recordId").toString());
        Integer peak = Integer.parseInt(session.get("peak").toString());
        // 获取总观看人数
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
        boolean updated = updateById(liveRoom);
        if (updated) {
            // 更新直播记录
            Long anchorId = liveRoom.getAnchorId();
            String totalKey = liveRoomProperties.getTotalPrefix() + roomId;
            Long total = stringRedisTemplate.opsForHyperLogLog().size(totalKey);
            VideoResource video = videoResourceService.lambdaQuery()
                    .select(VideoResource::getStartTime)
                    .eq(VideoResource::getId, recordId).list().get(0);
            log.info("开播时间: {}", video.getStartTime());
            video.setId(recordId);
            video.setWatchCount(total.intValue());
            video.setPeakViewers(peak);
            video.setEndTime(new Date());
            // 直播分类榜中减去当前直播间的人气热度值
            popularityUtil.endLive(String.valueOf(roomId));
            videoResourceService.updateById(video);
            // 删除缓存
            stringRedisTemplate
                    .delete(Arrays.asList(totalKey, liveRoomProperties.getCurrentPrefix() + roomId));
            stringRedisTemplate.opsForSet().remove(active, String.valueOf(roomId));
            stringRedisTemplate.opsForHash().delete(anchorMap, String.valueOf(anchorId));
            stringRedisTemplate.opsForZSet()
                    .remove(liveRoomProperties.getMainRank(), String.valueOf(roomId));
            String categoryKey = liveRoomProperties.getCategoryRoomsPrefix() + liveRoom.getCategoryId();
            stringRedisTemplate.opsForSet().remove(categoryKey, String.valueOf(roomId));
            stringRedisTemplate
                    .delete(liveRoomProperties.getChatBufferPrefix() + roomId);
        }
        return updated;
    }

    @Override
    public boolean dvr(SrsCallBackDTO dto) {
        String filePath = dto.getFile().replace("./objs", filePrefix);
        log.info("DVR回调 | 房间ID: {} | 录制文件: {}", dto.getStream(), filePath);
        long roomId = Long.parseLong(dto.getStream());
        String sessionKey = liveRoomProperties.getSessionPrefix() + roomId;
        Map<Object, Object> session = stringRedisTemplate.opsForHash().entries(sessionKey);
        log.info("DVR回调 | 房间ID: {} | 录制文件: {} | 会话信息: {}", roomId, dto.getFile(), session);
        Long recordId = Long.valueOf(session.get("recordId").toString());
        // 异步迁移视频至MinIO
        CompletableFuture.runAsync(() -> migrateVideoToMinio(recordId, filePath, dto.getStream()));
        // 设置会话缓存2秒后过期
        stringRedisTemplate.expire(sessionKey, 2, TimeUnit.SECONDS);
        return true;
    }

    @Override
    public List<LiveRoomRankVO> getPopularRooms() {
        String rankKey = liveRoomProperties.getMainRank();
        Set<String> roomIds = stringRedisTemplate
                .opsForZSet().reverseRange(rankKey, 0, 5);
        if (CollUtil.isEmpty(roomIds)) return List.of();
        List<String> roomIdList = roomIds.stream().filter(Objects::nonNull).toList();
        return popularityUtil.getPopularRoomVOS(roomIdList);

    }

    private void migrateVideoToMinio(Long recordId, String localPath, String stream) {
        // 获取视频封面
        File file = new File(localPath);
        VideoProcessResult processResult;
        try {
            processResult = videoProcessUtil.processVideo(file);
        } catch (EncoderException e) {
            throw new ApiException("视频处理失败");
        }

        long roomId = Long.parseLong(stream);
        Long uid = getById(roomId).getAnchorId();
        if (!file.exists()) {
            log.error("迁移失败 | 本地文件不存在: {}", localPath);
            return;
        }
        try {
            log.info("开始迁移视频至MinIO | RecordID: {} | 文件: {}", recordId, localPath);
            // 1. 生成 MinIO 中的文件名
            String fileName = stream + "-" + System.currentTimeMillis() + ".mp4";
            // 2. 调用你之前写的 uploadLocalFile 方法 (业务设为 "records")
            String videoUrl = minioTemplate.uploadLocalFile("records", fileName, localPath);
            String coverUrl = minioTemplate
                    .uploadLocalFile("records/cover"
                            , fileName.replace(".mp4", ".jpg")
                            , processResult.getCoverFile().getAbsolutePath());
            // 3. 更新数据库
            VideoResource video = new VideoResource();
            video.setId(recordId);
            video.setVideoUrl(videoUrl);
            video.setCoverUrl(coverUrl);
            video.setCreateTime(new Date());
            video.setDuration(processResult.getDuration());
            videoResourceService.updateById(video);
            log.info("迁移完成 | RecordID: {} | MinIO地址: {}", recordId, videoUrl);
            rabbitTemplate.convertAndSend(statsExchange, videoRoutingKey, Map
                    .of("userId", uid, "type", "add"));
            // 4. 清理本地临时文件
            if (file.delete() && processResult.getCoverFile().delete()) {
                log.info("清理本地文件成功: {}", localPath);
            }
        } catch (Exception e) {
            log.error("迁移过程发生异常 | RecordID: {}", recordId, e);
            // 这里可以视情况决定是否将数据库状态标记为“上传失败”
        }
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




