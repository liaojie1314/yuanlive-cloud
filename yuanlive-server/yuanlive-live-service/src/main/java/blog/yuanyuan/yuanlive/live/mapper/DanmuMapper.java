package blog.yuanyuan.yuanlive.live.mapper;

import blog.yuanyuan.yuanlive.entity.live.entity.Danmu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 弹幕 Mapper 接口
 */
@Mapper
public interface DanmuMapper extends BaseMapper<Danmu> {
    // 继承 BaseMapper
}