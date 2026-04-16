package blog.yuanyuan.yuanlive.user.mapper;

import blog.yuanyuan.yuanlive.entity.user.entity.SysUser;
import blog.yuanyuan.yuanlive.user.domain.dto.UserQueryDTO;
import blog.yuanyuan.yuanlive.user.domain.vo.UserVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author frodepu
* @description 针对表【sys_user(用户表)】的数据库操作Mapper
* @createDate 2025-12-26 15:51:16
* @Entity blog.yuanyuan.yuanlive.entity.user.entity.SysUser
*/
public interface SysUserMapper extends BaseMapper<SysUser> {

    UserVO getUserVOByID(Long userId);

    List<UserVO> getUserVOByIDs(@Param("userIds") List<Long> userIds);
}




