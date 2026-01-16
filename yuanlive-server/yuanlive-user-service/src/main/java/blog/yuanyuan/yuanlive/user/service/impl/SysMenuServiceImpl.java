package blog.yuanyuan.yuanlive.user.service.impl;

import blog.yuanyuan.yuanlive.common.exception.ApiException;
import blog.yuanyuan.yuanlive.entity.user.entity.SysMenu;
import blog.yuanyuan.yuanlive.user.domain.dto.MenuDTO;
import blog.yuanyuan.yuanlive.user.domain.vo.MenuVO;
import blog.yuanyuan.yuanlive.user.mapper.SysMenuMapper;
import blog.yuanyuan.yuanlive.user.service.SysMenuService;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author frodepu
* @description 针对表【sys_menu(菜单权限表)】的数据库操作Service实现
* @createDate 2025-12-26 15:51:16
*/
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu>
    implements SysMenuService{

    @Override
    public List<MenuVO> treeList() {
        List<SysMenu> allMenus = list();
        List<MenuVO> voList = allMenus.stream()
                .map(menu -> BeanUtil.copyProperties(menu, MenuVO.class))
                .collect(Collectors.toList());
        return buildTree(voList, 0L);
    }

    private List<MenuVO> buildTree(List<MenuVO> menus, Long parentId) {
        return menus.stream()
                .filter(menu -> parentId.equals(menu.getParentId()))
                .map(menu -> {
                    menu.setChildren(buildTree(menus, menu.getMenuId()));
                    return menu;
                })
                .sorted(Comparator.comparing(MenuVO::getSort, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    @Override
    public boolean addMenu(MenuDTO menuDTO) {
        SysMenu menu = BeanUtil.copyProperties(menuDTO, SysMenu.class);
        return save(menu);
    }

    @Override
    public boolean updateMenu(MenuDTO menuDTO) {
        if (menuDTO.getMenuId() == null) {
            throw new ApiException("菜单ID不能为空");
        }
        SysMenu menu = BeanUtil.copyProperties(menuDTO, SysMenu.class);
        return updateById(menu);
    }

    @Override
    public boolean removeMenuById(Long menuId) {
        boolean exists = lambdaQuery().eq(SysMenu::getParentId, menuId).exists();
        if (exists) {
            throw new ApiException("存在子菜单，不允许删除");
        }
        return removeById(menuId);
    }
}




