package blog.yuanyuan.yuanlive.user.service;

import blog.yuanyuan.yuanlive.entity.user.entity.UserFollow;
import blog.yuanyuan.yuanlive.user.domain.dto.UserFollowDTO;
import blog.yuanyuan.yuanlive.entity.user.vo.UserFollowVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;


public interface UserFollowService extends IService<UserFollow> {

    /**
     * 关注用户
     */
    Boolean followUser(UserFollowDTO userFollowDTO);

    /**
     * 取消关注用户
     */
    Boolean unfollowUser(Long userId, Long followUserId);

    /**
     * 获取用户的粉丝列表
     */
    List<UserFollowVO> getFollowers(Long followUserId);

    /**
     * 获取用户关注的列表
     */
    List<UserFollowVO> getFollowing(Long userId);

    /**
     * 检查用户是否关注了目标用户
     */
    Boolean checkFollowing(Long userId, Long followUserId);

    List<UserFollowVO> getFollowingLive(Long userId);
}
