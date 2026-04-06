package blog.yuanyuan.yuanlive.live.service.impl;

import blog.yuanyuan.yuanlive.entity.live.entity.Danmu;
import blog.yuanyuan.yuanlive.live.mapper.DanmuMapper;
import blog.yuanyuan.yuanlive.live.service.IDanmuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import java.util.Date;

/**
 * 弹幕服务实现类
 */
@Service
public class DanmuServiceImpl extends ServiceImpl<DanmuMapper, Danmu> implements IDanmuService {

    @Override
    public Boolean sendDanmu(Danmu danmu) {
        // 1. 初始化默认值 (前端可能不传，我们需要在后端补全)
        danmu.setLikeCount(0); // 刚发的弹幕点赞数为0
        danmu.setStatus(0);    // 状态 0 代表正常显示
        danmu.setCreateTime(new Date()); // 设置当前发送时间

        // 2. 实际项目中，userId 应该从当前登录的 Token 里获取（比如通过拦截器或网关传递）。
        // 这里如果是单纯测接口，我们可以先假设前端传了 userId，或者你以后再接入统一的获取用户逻辑。

        // 3. 调用 MyBatis-Plus 提供的 save 方法保存到数据库
        return this.save(danmu);
    }
}