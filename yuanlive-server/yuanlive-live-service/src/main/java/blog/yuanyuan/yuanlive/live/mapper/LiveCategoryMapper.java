package blog.yuanyuan.yuanlive.live.mapper;

import blog.yuanyuan.yuanlive.entity.live.entity.LiveCategory;
import blog.yuanyuan.yuanlive.live.domain.vo.HotCategoryVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author frodepu
* @description 针对表【live_category(直播分类表)】的数据库操作Mapper
* @createDate 2026-01-27 14:55:18
* @Entity blog.yuanyuan.yuanlive.entity.live.entity.LiveCategory
*/
public interface LiveCategoryMapper extends BaseMapper<LiveCategory> {

    List<HotCategoryVO> getHotCategories(@Param("categoryIds") List<Integer> categoryIds);
}




