package blog.yuanyuan.yuanlive.live.service;

import blog.yuanyuan.yuanlive.entity.live.entity.Danmu;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 弹幕服务接口
 */
public interface IDanmuService extends IService<Danmu> {

    /**
     * 发送弹幕的业务逻辑
     * @param danmu 弹幕实体（前端传来的数据）
     * @return 是否发送成功
     */
    Boolean sendDanmu(Danmu danmu);
}