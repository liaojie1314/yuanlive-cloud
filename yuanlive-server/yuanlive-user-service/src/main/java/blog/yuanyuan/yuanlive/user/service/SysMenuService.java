package blog.yuanyuan.yuanlive.user.service;

import blog.yuanyuan.yuanlive.entity.user.entity.SysMenu;
import com.baomidou.mybatisplus.extension.service.IService;

import blog.yuanyuan.yuanlive.user.domain.dto.MenuDTO;
import blog.yuanyuan.yuanlive.user.domain.vo.MenuVO;

import java.util.List;

/**
* @author frodepu
* @description 针对表【sys_menu(菜单权限表)】的数据库操作Service
* @createDate 2025-12-26 15:51:16
*/
public interface SysMenuService extends IService<SysMenu> {

    List<MenuVO> treeList();

    boolean addMenu(MenuDTO menuDTO);

    boolean updateMenu(MenuDTO menuDTO);

    boolean removeMenuById(Long menuId);
}
