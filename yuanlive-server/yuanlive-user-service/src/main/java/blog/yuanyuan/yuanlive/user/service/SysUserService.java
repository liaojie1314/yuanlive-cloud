package blog.yuanyuan.yuanlive.user.service;

import blog.yuanyuan.yuanlive.common.result.ResultPage;
import blog.yuanyuan.yuanlive.entity.user.entity.SysUser;
import blog.yuanyuan.yuanlive.user.domain.dto.UserQueryDTO;
import blog.yuanyuan.yuanlive.user.domain.dto.UserRoleDTO;
import blog.yuanyuan.yuanlive.user.domain.vo.RouterVO;
import blog.yuanyuan.yuanlive.user.domain.vo.UserVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author frodepu
* @description 针对表【sys_user(用户表)】的数据库操作Service
* @createDate 2025-12-26 15:51:16
*/
public interface SysUserService extends IService<SysUser> {

//    /**
//     * 修改用户
//     * @param userDTO 用户信息
//     * @return 结果
//     */
//    boolean updateUser(UserDTO userDTO);
//
//    /**
//     * 批量删除用户
//     * @param userIds 用户ID集合
//     * @return 结果
//     */
//    boolean deleteUsers(List<Long> userIds);
//
    /**
     * 根据ID获取用户详情
     * @param userId 用户ID
     * @return 用户详情
     */
    UserVO getUserById(Long userId);

    /**
     * 分页查询用户列表
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    ResultPage<UserVO> pageUsers(UserQueryDTO queryDTO);

    /**
     * 为用户分配角色
     * @param userRoleDTO 用户角色信息
     * @return 结果
     */
    boolean assignRoles(UserRoleDTO userRoleDTO);

    List<RouterVO> getRouters();

    /**
     * 校验token
     * @param token token
     * @return 用户ID
     */
    Long checkToken(String token);

    UserVO getUserInfo();
}
