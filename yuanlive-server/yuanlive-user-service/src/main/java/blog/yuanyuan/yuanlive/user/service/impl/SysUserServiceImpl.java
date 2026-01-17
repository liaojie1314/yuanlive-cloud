package blog.yuanyuan.yuanlive.user.service.impl;

import blog.yuanyuan.yuanlive.common.result.ResultPage;
import blog.yuanyuan.yuanlive.entity.user.entity.SysRole;
import blog.yuanyuan.yuanlive.entity.user.entity.SysUser;
import blog.yuanyuan.yuanlive.entity.user.entity.SysUserRole;
import blog.yuanyuan.yuanlive.user.domain.dto.UserQueryDTO;
import blog.yuanyuan.yuanlive.user.domain.dto.UserRoleDTO;
import blog.yuanyuan.yuanlive.user.domain.vo.UserVO;
import blog.yuanyuan.yuanlive.user.mapper.SysUserMapper;
import blog.yuanyuan.yuanlive.user.service.SysRoleService;
import blog.yuanyuan.yuanlive.user.service.SysUserRoleService;
import blog.yuanyuan.yuanlive.user.service.SysUserService;
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

    private void insertUserRole(Long userId, List<Long> roleIds) {
        List<SysUserRole> userRoles = roleIds.stream().map(roleId -> {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            return userRole;
        }).collect(Collectors.toList());
        userRoleService.saveBatch(userRoles);
    }
}




