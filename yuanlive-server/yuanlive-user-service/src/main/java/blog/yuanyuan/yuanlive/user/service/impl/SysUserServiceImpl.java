package blog.yuanyuan.yuanlive.user.service.impl;

import blog.yuanyuan.yuanlive.common.exception.ApiException;
import blog.yuanyuan.yuanlive.common.result.ResultPage;
import blog.yuanyuan.yuanlive.entity.user.entity.SysMenu;
import blog.yuanyuan.yuanlive.entity.user.entity.SysRole;
import blog.yuanyuan.yuanlive.entity.user.entity.SysUser;
import blog.yuanyuan.yuanlive.entity.user.entity.SysUserRole;
import blog.yuanyuan.yuanlive.user.domain.dto.UserQueryDTO;
import blog.yuanyuan.yuanlive.user.domain.dto.UserRoleDTO;
import blog.yuanyuan.yuanlive.user.domain.vo.RouterVO;
import blog.yuanyuan.yuanlive.user.domain.vo.UserVO;
import blog.yuanyuan.yuanlive.user.mapper.SysUserMapper;
import blog.yuanyuan.yuanlive.user.service.SysRoleService;
import blog.yuanyuan.yuanlive.user.service.SysUserRoleService;
import blog.yuanyuan.yuanlive.user.service.SysUserService;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
* @author frodepu
* @description 针对表【sys_user(用户表)】的数据库操作Service实现
* @createDate 2025-12-26 15:51:16
*/
@Service
@Slf4j
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser>
    implements SysUserService {


    @Resource
    SysUserRoleService userRoleService;
    @Resource
    SysRoleService roleService;
    @Resource
    SysUserMapper userMapper;

//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public boolean updateUser(UserDTO userDTO) {
//        SysUser sysUser = new SysUser();
//        BeanUtils.copyProperties(userDTO, sysUser);
//
//        // 如果修改了密码，需要加密
//        if (StringUtils.hasText(userDTO.getPassword())) {
//            sysUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
//        } else {
//            // 不修改密码则置为null
//            sysUser.setPassword(null);
//        }
//
//        boolean updated = updateById(sysUser);
//        if (updated && userDTO.getRoleIds() != null) {
//            // 删除原有关联
//            userRoleService.remove(new LambdaQueryWrapper<SysUserRole>()
//                    .eq(SysUserRole::getUserId, sysUser.getUid()));
//            // 插入新关联
//            if (!userDTO.getRoleIds().isEmpty()) {
//                insertUserRole(sysUser.getUid(), userDTO.getRoleIds());
//            }
//        }
//        return updated;
//    }

//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public boolean deleteUsers(List<Long> userIds) {
//        // 删除用户
//        boolean removed = removeByIds(userIds);
//        if (removed) {
//            // 删除用户角色关联
//            userRoleService.remove(new LambdaQueryWrapper<SysUserRole>()
//                    .in(SysUserRole::getUserId, userIds));
//        }
//        return removed;
//    }

    @Override
    public UserVO getUserById(Long userId) {
        return userMapper.getUserVOByID(userId);
    }

    @Override
    public ResultPage<UserVO> pageUsers(UserQueryDTO queryDTO) {
        // 先查询Users
        Page<SysUser> page = lambdaQuery()
                .eq(queryDTO.getDelFlag() != null, SysUser::getDelFlag, queryDTO.getDelFlag())
                .like(StrUtil.isNotBlank(queryDTO.getUsername()), SysUser::getUsername, queryDTO.getUsername())
                .like(StrUtil.isNotBlank(queryDTO.getEmail()), SysUser::getEmail, queryDTO.getEmail())
                .like(StrUtil.isNotBlank(queryDTO.getPhone()), SysUser::getPhone, queryDTO.getPhone())
                .eq(queryDTO.getStatus() != null, SysUser::getStatus, queryDTO.getStatus())
                .page(new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()));
        List<Long> userIds = page.getRecords()
                .stream().map(SysUser::getUid).toList();
        List<UserVO> vos = userMapper.getUserVOByIDs(userIds);
        Page<UserVO> voPage = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize(), page.getTotal());
        voPage.setRecords(vos);
        return ResultPage.of(voPage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignRoles(UserRoleDTO userRoleDTO) {
        // 先删除原有关联
        userRoleService.remove(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userRoleDTO.getUserId()));
        // 插入新关联
        insertUserRole(userRoleDTO.getUserId(), userRoleDTO.getRoleIds());
        return true;
    }

    @Override
    public List<RouterVO> getRouters() {
        long uid = StpUtil.getLoginIdAsLong();
        UserVO userVO = getUserById(uid);
        List<SysRole> roles = userVO.getRoles();
        List<Long> roleIds = roles.stream().map(SysRole::getRoleId).toList();
        List<SysMenu> menus = roleService.getRolesMenus(roleIds);
        return buildRouterVO(menus);
    }

    @Override
    public Long checkToken(String token) {
        try {
            String userId = (String)StpUtil.getLoginIdByToken(token);
            Long uid = Long.valueOf(userId);
            log.info("用户id{}", uid);
            return uid;
        } catch (NumberFormatException e) {
            throw new ApiException("Token 无效或过期");
        }
    }

    @Override
    public UserVO getUserInfo() {
        long uid = StpUtil.getLoginIdAsLong();
        return getUserById(uid);
    }

    private void insertUserRole(Long userId, List<Long> roleIds) {
        List<SysUserRole> userRoles = roleIds.stream().map(roleId -> {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            return userRole;
        }).collect(Collectors.toList());
        userRoleService.saveBatch(userRoles);
    }

    /**
     * 构建路由树形结构
     * @param menus 菜单列表
     * @return 路由VO列表
     */
    private List<RouterVO> buildRouterVO(List<SysMenu> menus) {
        if (menus == null || menus.isEmpty()) {
            log.warn("菜单列表为空，返回空路由");
            return List.of();
        }

        // 过滤出目录和菜单，排除按钮类型
        List<SysMenu> filteredMenus = menus.stream()
                .filter(menu -> !"F".equals(menu.getMenuType()))
                .toList();

        // 从根节点开始构建树（parentId == 0），传递完整的菜单列表用于提取按钮权限
        return buildRouterTree(filteredMenus, menus, 0L);
    }

    /**
     * 递归构建路由树
     * @param filteredMenus 过滤后的菜单列表（不含按钮）
     * @param allMenus 完整的菜单列表（包含按钮，用于提取权限）
     * @param parentId 父节点ID
     * @return 当前层级的路由列表
     */
    private List<RouterVO> buildRouterTree(List<SysMenu> filteredMenus, List<SysMenu> allMenus, Long parentId) {
        return filteredMenus.stream()
                .filter(menu -> parentId.equals(menu.getParentId()))
                .map(menu -> {
                    // 转换为 RouterVO
                    RouterVO router = RouterVO.builder()
                            .path(menu.getPath())
                            .name(menu.getName())
                            .component(menu.getComponent())
                            .meta(buildMetaVO(menu, allMenus))
                            .build();

                    // 递归构建子路由
                    List<RouterVO> children = buildRouterTree(filteredMenus, allMenus, menu.getMenuId());
                    if (!children.isEmpty()) {
                        router.setChildren(children);
                    }

                    return router;
                })
                .sorted((r1, r2) -> {
                    Integer sort1 = r1.getMeta() != null ? r1.getMeta().getRank() : null;
                    Integer sort2 = r2.getMeta() != null ? r2.getMeta().getRank() : null;
                    if (sort1 == null && sort2 == null) return 0;
                    if (sort1 == null) return 1;
                    if (sort2 == null) return -1;
                    return sort1.compareTo(sort2);
                })
                .collect(Collectors.toList());
    }

    /**
     * 构建路由元数据
     * @param menu 当前菜单
     * @param allMenus 所有菜单列表（用于查找子按钮权限）
     * @return MetaVO对象
     */
    private RouterVO.MetaVO buildMetaVO(SysMenu menu, List<SysMenu> allMenus) {
        // 查找当前菜单下的所有按钮权限
        List<String> auths = allMenus.stream()
                .filter(m -> "F".equals(m.getMenuType()) && menu.getMenuId().equals(m.getParentId()))
                .map(SysMenu::getPerms)
                .filter(StringUtils::hasText)
                .toList();

        return RouterVO.MetaVO.builder()
                .title(menu.getTitle())
                .icon(menu.getIcon())
                .rank(menu.getSort())
                .auths(auths.isEmpty() ? null : auths)
                .showLink(menu.getIsVisible() != null && menu.getIsVisible() == 1)
                .keepAlive(menu.getIsCache() != null && menu.getIsCache() == 1)
                .build();
    }
}




