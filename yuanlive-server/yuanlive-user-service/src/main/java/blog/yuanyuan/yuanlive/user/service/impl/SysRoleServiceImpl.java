package blog.yuanyuan.yuanlive.user.service.impl;

import blog.yuanyuan.yuanlive.common.exception.ApiException;
import blog.yuanyuan.yuanlive.common.result.ResultPage;
import blog.yuanyuan.yuanlive.entity.user.entity.SysMenu;
import blog.yuanyuan.yuanlive.entity.user.entity.SysRole;
import blog.yuanyuan.yuanlive.entity.user.entity.SysRoleMenu;
import blog.yuanyuan.yuanlive.user.domain.dto.RoleDTO;
import blog.yuanyuan.yuanlive.user.domain.dto.RoleQueryDTO;
import blog.yuanyuan.yuanlive.user.domain.vo.RoleVO;
import blog.yuanyuan.yuanlive.user.mapper.SysRoleMapper;
import blog.yuanyuan.yuanlive.user.service.SysMenuService;
import blog.yuanyuan.yuanlive.user.service.SysRoleMenuService;
import blog.yuanyuan.yuanlive.user.service.SysRoleService;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
* @author frodepu
* @description 针对表【sys_role(角色表)】的数据库操作Service实现
* @createDate 2025-12-26 15:51:16
*/
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole>
    implements SysRoleService {

    @Resource
    SysRoleMenuService roleMenuService;
    @Resource
    SysRoleMapper sysRoleMapper;
    @Resource
    SysMenuService menuService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveRole(RoleDTO roleDTO) {
        SysRole sysRole = new SysRole();
        BeanUtils.copyProperties(roleDTO, sysRole);
        boolean saved = save(sysRole);
        if (saved && roleDTO.getMenuIds() != null && !roleDTO.getMenuIds().isEmpty()) {
            insertRoleMenu(sysRole.getRoleId(), roleDTO.getMenuIds());
        }
        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRole(RoleDTO roleDTO) {
        SysRole sysRole = new SysRole();
        BeanUtils.copyProperties(roleDTO, sysRole);
        boolean updated = updateById(sysRole);
        if (updated) {
            // 删除原有关联
            roleMenuService.remove(new LambdaQueryWrapper<SysRoleMenu>()
                    .eq(SysRoleMenu::getRoleId, sysRole.getRoleId()));
            // 插入新关联
            if (roleDTO.getMenuIds() != null && !roleDTO.getMenuIds().isEmpty()) {
                insertRoleMenu(sysRole.getRoleId(), roleDTO.getMenuIds());
            }
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRoles(List<Long> roleIds) {
        // 删除角色
        boolean removed = removeByIds(roleIds);
        if (removed) {
            // 删除角色菜单关联
            roleMenuService.remove(new LambdaQueryWrapper<SysRoleMenu>()
                    .in(SysRoleMenu::getRoleId, roleIds));
        }
        return removed;
    }

    @Override
    public RoleVO getRoleById(Long roleId) {
        return sysRoleMapper.getRoleVOByID(roleId);
    }

    @Override
    public ResultPage<RoleVO> pageRoles(RoleQueryDTO queryDTO) {
        Page<SysRole> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        lambdaQuery()
                .like(StrUtil.isNotBlank(queryDTO.getRoleName()), SysRole::getRoleName, queryDTO.getRoleName())
                .like(StrUtil.isNotBlank(queryDTO.getRoleKey()), SysRole::getRoleKey, queryDTO.getRoleKey())
                .eq(queryDTO.getStatus() != null, SysRole::getStatus, queryDTO.getStatus())
                .page(page);
        List<RoleVO> vos = page.getRecords().stream()
                .map(role -> {
                    return BeanUtil.copyProperties(role, RoleVO.class);
                }).toList();
        Page<RoleVO> pageVO = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        pageVO.setRecords(vos);
        return ResultPage.of(pageVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean grantAll(String roleKey) {
        // 1. 根据 Key 查询角色
        SysRole role = this.lambdaQuery().eq(SysRole::getRoleKey, roleKey).one();
        if (role == null) {
            throw new ApiException("角色不存在: " + roleKey);
        }
        Long roleId = role.getRoleId();
        // 2. 查询所有菜单的 ID 列表
        // 只查 ID 列，性能更好
        List<Long> allMenuIds = menuService.listObjs(
                new LambdaQueryWrapper<SysMenu>().select(SysMenu::getMenuId)
        );
        if (allMenuIds.isEmpty()) {
            return false;
        }
        // 3. 删除该角色原有的所有权限 (清理旧数据)
        roleMenuService.remove(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId)
        );
        // 4. 批量插入
        insertRoleMenu(roleId, allMenuIds);
        return true;
    }

    public List<SysMenu> getRolesMenus(List<Long> roleIds) {
        return sysRoleMapper.getRolesMenus(roleIds);
    }

    private void insertRoleMenu(Long roleId, List<Long> menuIds) {
        List<SysRoleMenu> roleMenus = menuIds.stream().map(menuId -> {
            SysRoleMenu roleMenu = new SysRoleMenu();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            return roleMenu;
        }).collect(Collectors.toList());
        roleMenuService.saveBatch(roleMenus);
    }
}




