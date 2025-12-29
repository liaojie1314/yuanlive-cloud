package blog.yuanyuan.yuanlive.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import blog.yuanyuan.yuanlive.entity.user.entity.SysMenu;
import blog.yuanyuan.yuanlive.user.service.SysMenuService;
import blog.yuanyuan.yuanlive.user.mapper.SysMenuMapper;
import org.springframework.stereotype.Service;

/**
* @author frodepu
* @description 针对表【sys_menu(菜单权限表)】的数据库操作Service实现
* @createDate 2025-12-26 15:51:16
*/
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu>
    implements SysMenuService{

    @Override
    public boolean addMenu(SysMenu menu) {
        return save(menu);
    }
}




