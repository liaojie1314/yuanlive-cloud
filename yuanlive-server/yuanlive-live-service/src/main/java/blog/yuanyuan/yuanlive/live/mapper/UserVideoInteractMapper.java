package blog.yuanyuan.yuanlive.live.mapper;

import blog.yuanyuan.yuanlive.entity.live.entity.UserVideoInteract;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
* @author frodepu
* @description 针对表【user_video_interact(记录用户对视频的点赞、分享、推荐记录)】的数据库操作Mapper
* @createDate 2026-03-12 14:36:23
* @Entity blog.yuanyuan.yuanlive.entity.live.entity.UserVideoInteract
*/
public interface UserVideoInteractMapper extends BaseMapper<UserVideoInteract> {

    void upsertAction(@Param("id") long id, @Param("uid") Long uid,
                      @Param("vid") Long vid, @Param("column") String column);

    void undoAction(@Param("uid") Long uid, @Param("vid") Long vid,
                    @Param("column") String column);
}




