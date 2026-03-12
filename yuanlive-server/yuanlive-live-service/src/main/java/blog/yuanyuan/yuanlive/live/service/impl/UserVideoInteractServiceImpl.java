package blog.yuanyuan.yuanlive.live.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import blog.yuanyuan.yuanlive.entity.live.entity.UserVideoInteract;
import blog.yuanyuan.yuanlive.live.service.UserVideoInteractService;
import blog.yuanyuan.yuanlive.live.mapper.UserVideoInteractMapper;
import org.springframework.stereotype.Service;

/**
* @author frodepu
* @description 针对表【user_video_interact(记录用户对视频的点赞、分享、推荐记录)】的数据库操作Service实现
* @createDate 2026-03-12 14:36:23
*/
@Service
public class UserVideoInteractServiceImpl extends ServiceImpl<UserVideoInteractMapper, UserVideoInteract>
    implements UserVideoInteractService{

}




