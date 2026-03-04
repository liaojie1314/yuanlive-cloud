package blog.yuanyuan.yuanlive.live.mapper;

import blog.yuanyuan.yuanlive.entity.live.entity.LiveRoom;
import blog.yuanyuan.yuanlive.entity.live.vo.LiveRoomRankVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author frodepu
* @description 针对表【live_room(直播间表)】的数据库操作 Mapper
* @createDate 2026-01-27 14:55:18
* @Entity blog.yuanyuan.yuanlive.entity.live.entity.LiveRoom
*/
public interface LiveRoomMapper extends BaseMapper<LiveRoom> {

    /**
     * 搜索直播间（支持标题、主播名、分类名模糊匹配）
     * @param keyword 搜索关键词
     * @return 匹配的直播间列表
     */
    List<LiveRoomRankVO> searchLiveRooms(IPage<LiveRoomRankVO> page, @Param("keyword") String keyword);
}




