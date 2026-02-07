package blog.yuanyuan.yuanlive.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import blog.yuanyuan.yuanlive.entity.user.entity.UserStats;
import blog.yuanyuan.yuanlive.user.service.UserStatsService;
import blog.yuanyuan.yuanlive.user.mapper.UserStatsMapper;
import org.springframework.stereotype.Service;

/**
* @author frodepu
* @description 针对表【user_stats(用户统计表)】的数据库操作Service实现
* @createDate 2026-02-07 17:39:23
*/
@Service
public class UserStatsServiceImpl extends ServiceImpl<UserStatsMapper, UserStats>
    implements UserStatsService{

}




