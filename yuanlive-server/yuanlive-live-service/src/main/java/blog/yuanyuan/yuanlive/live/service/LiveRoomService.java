package blog.yuanyuan.yuanlive.live.service;

import blog.yuanyuan.yuanlive.common.result.ResultPage;
import blog.yuanyuan.yuanlive.entity.live.entity.LiveRoom;
import blog.yuanyuan.yuanlive.live.domain.dto.LiveRoomDTO;
import blog.yuanyuan.yuanlive.live.domain.dto.LiveRoomQueryDTO;
import blog.yuanyuan.yuanlive.live.domain.dto.SrsCallBackDTO;
import blog.yuanyuan.yuanlive.live.domain.vo.LiveRoomDetailVO;
import blog.yuanyuan.yuanlive.live.domain.vo.LiveRoomVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author frodepu
* @description 针对表【live_room(直播间表)】的数据库操作Service
* @createDate 2026-01-27 14:55:18
*/
public interface LiveRoomService extends IService<LiveRoom> {

    /**
     * 创建直播间
     * @param roomDTO 直播间信息
     * @param anchorId 主播ID
     * @return 是否成功
     */
    boolean createRoom(LiveRoomDTO roomDTO, Long anchorId);

    /**
     * 修改直播间信息
     * @param roomDTO 直播间信息
     * @param anchorId 主播ID
     * @return 是否成功
     */
    boolean updateRoom(LiveRoomDTO roomDTO, Long anchorId);

    /**
     * 开始直播
      * @param dto 直播间信息
     * @return 是否成功
     */
    boolean startLive(SrsCallBackDTO dto);

    /**
     * 结束直播
     * @param dto 关闭直播回调
     * @return 是否成功
     */
    boolean endLive(SrsCallBackDTO dto);

    /**
     * 获取直播间详情
     * @param roomId 直播间ID
     * @return 直播间详情
     */
    LiveRoomDetailVO getRoomDetail(Long roomId);

    /**
     * 获取主播的直播间信息
     * @param anchorId 主播ID
     * @return 直播间信息
     */
    LiveRoomVO getAnchorRoom(Long anchorId);

    /**
     * 分页查询直播间列表
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    ResultPage<LiveRoomVO> pageRooms(LiveRoomQueryDTO queryDTO);

    boolean dvr(SrsCallBackDTO dto);
}
