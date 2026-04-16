package blog.yuanyuan.yuanlive.user.service.impl;

import blog.yuanyuan.yuanlive.common.exception.ApiException;
import blog.yuanyuan.yuanlive.common.result.Result;
import blog.yuanyuan.yuanlive.common.result.ResultPage;
import blog.yuanyuan.yuanlive.common.util.IdCardUtil;
import blog.yuanyuan.yuanlive.common.util.InterestUtil;
import blog.yuanyuan.yuanlive.entity.live.dto.LiveRoomDTO;
import blog.yuanyuan.yuanlive.entity.live.dto.SearchQueryDTO;
import blog.yuanyuan.yuanlive.entity.live.vo.SearchVO;
import blog.yuanyuan.yuanlive.entity.user.dto.AnchorApplyDTO;
import blog.yuanyuan.yuanlive.entity.user.entity.*;
import blog.yuanyuan.yuanlive.feign.live.LiveFeignClient;
import blog.yuanyuan.yuanlive.user.domain.dto.PasswordDTO;
import blog.yuanyuan.yuanlive.user.domain.dto.UserQueryDTO;
import blog.yuanyuan.yuanlive.user.domain.dto.UserRoleDTO;
import blog.yuanyuan.yuanlive.user.domain.dto.UserDTO;
import blog.yuanyuan.yuanlive.common.enums.BehaviorType;
import blog.yuanyuan.yuanlive.user.domain.vo.RouterVO;
import blog.yuanyuan.yuanlive.user.domain.vo.SearchHotVO;
import blog.yuanyuan.yuanlive.user.domain.vo.SearchRecommendVO;
import blog.yuanyuan.yuanlive.user.domain.vo.UserVO;
import blog.yuanyuan.yuanlive.user.mapper.SysUserMapper;
import blog.yuanyuan.yuanlive.user.mapper.UserFollowMapper;
import blog.yuanyuan.yuanlive.common.properties.SearchProperties;
import blog.yuanyuan.yuanlive.user.service.*;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import me.ahoo.cosid.provider.IdGeneratorProvider;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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
    private SysUserRoleService userRoleService;
    @Resource
    private AnchorApplyService anchorApplyService;
    @Resource
    private SysRoleService roleService;
    @Resource
    private UserStatsService userStatsService;
    @Resource
    private SearchHistoryService searchHistoryService;
    @Resource
    private SysUserMapper userMapper;
    @Resource
    private UserFollowMapper userFollowMapper;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private IdGeneratorProvider idGeneratorProvider;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private SearchProperties searchProperties;
    @Resource
    private LiveFeignClient liveFeignClient;
    @Resource
    private InterestUtil interestUtil;
    @Resource
    private IdCardUtil idCardUtil;
    @Value("${redis-key.anchor-map.key}")
    private String anchorMap;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(UserDTO userDTO) {
        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(userDTO, sysUser);

        boolean updated = updateById(sysUser);
        if (updated && CollUtil.isNotEmpty(userDTO.getRoleIds())) {
            UserRoleDTO roleDTO = new UserRoleDTO(userDTO.getUid(), userDTO.getRoleIds());
            assignRoles(roleDTO);
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteOrRestoreUsers(List<Long> userIds) {
        // 逻辑删除或恢复用户
        return lambdaUpdate()
                .setSql("del_flag = 1 - del_flag")
                .in(SysUser::getUid, userIds)
                .update();
    }

    @Override
    public UserVO getUserById(Long userId) {
        return userMapper.getUserVOByID(userId);
    }

    @Override
    public ResultPage<UserVO> pageUsers(UserQueryDTO queryDTO) {
        // 先查询Users
        Page<SysUser> page = lambdaQuery()
                .eq(queryDTO.getDelFlag() != null && queryDTO.getDelFlag() >= 0,
                        SysUser::getDelFlag, queryDTO.getDelFlag())
                .like(StrUtil.isNotBlank(queryDTO.getUsername()), SysUser::getUsername, queryDTO.getUsername())
                .like(StrUtil.isNotBlank(queryDTO.getEmail()), SysUser::getEmail, queryDTO.getEmail())
                .like(StrUtil.isNotBlank(queryDTO.getPhone()), SysUser::getPhone, queryDTO.getPhone())
                .eq(queryDTO.getStatus() != null, SysUser::getStatus, queryDTO.getStatus())
                .eq(queryDTO.getGender() != null, SysUser::getGender, queryDTO.getGender())
                .page(new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()));
        List<Long> userIds = page.getRecords()
                .stream().map(SysUser::getUid).toList();
        if (CollUtil.isEmpty(userIds)) {
            return ResultPage.empty();
        }
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
    public SysUser checkToken(String token) {
        try {
            String userId = (String)StpUtil.getLoginIdByToken(token);
            Long uid = Long.valueOf(userId);
            log.info("用户id{}", uid);
            return getById(uid);
        } catch (NumberFormatException e) {
            throw new ApiException("Token 无效或过期");
        }
    }

    @Override
    public UserVO getUserInfo() {
        long uid = StpUtil.getLoginIdAsLong();
        UserVO user = getUserById(uid);
        user.setDevice(StpUtil.getLoginDeviceType());
        UserStats stats = userStatsService.getById(uid);
        // 获取关注列表中正在直播的人数
        Integer followingLiveCount = getFollowingLiveCount(uid);
        stats.setFollowingLiveCount(followingLiveCount);
        user.setUserStats(stats);
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveUser(UserDTO userDTO) {
        SysUser user = BeanUtil.copyProperties(userDTO, SysUser.class);
        user.setUid(idGeneratorProvider.getRequired("safe-js").generate());
        if (StrUtil.isNotBlank(userDTO.getPassword())) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        if (save(user)) {
            if (CollUtil.isNotEmpty(userDTO.getRoleIds())) {
                insertUserRole(user.getUid(), userDTO.getRoleIds());
            }
            return true;
        }
        return false;
    }

    @Override
    public Boolean changePassword(PasswordDTO passwordDTO) {
        if (!Objects.equals(passwordDTO.getPassword(), passwordDTO.getConfirmPassword())) {
            throw new ApiException("两次输入的密码不一致");
        }
        String password = passwordEncoder.encode(passwordDTO.getPassword());
        SysUser user = new SysUser();
        user.setUid(passwordDTO.getUid());
        user.setPassword(password);
        return updateById(user);
    }

    @Override
    public Boolean updateStatus(Long uid) {
        return lambdaUpdate()
                .setSql("status = 1 - status")
                .eq(SysUser::getUid, uid)
                .update();
    }

    @Override
    public Boolean restoreOrDelete(Long uid) {
        SysUser user = getById(uid);
        user.setDelFlag(user.getDelFlag() == 1 ? 0 : 1);
        return updateById(user);
    }

    @Override
    public ResultPage<SearchVO> search(SearchQueryDTO queryDTO) {
        long uid = StpUtil.getLoginIdAsLong();
        String trimKeyword = queryDTO.getQuery().trim();
        Integer categoryId = liveFeignClient.getCategoryIdBySearch(queryDTO.getQuery()).getData();
        Integer topCategoryId = null;
        topCategoryId = categoryId != null ? categoryId : topCategoryId;
        ResultPage<SearchVO> result = liveFeignClient.search(queryDTO);
        if (topCategoryId == null) {
            if (!result.getList().isEmpty()) {
                Map<Integer, Long> map = result.getList().stream()
                        .map(searchVO -> searchVO.isCheckRoom()
                                ? searchVO.getLiveRoom().getCategoryId()
                                : searchVO.getVideo().getCategoryId())
                        .filter(Objects::nonNull)
                        .collect(Collectors.groupingBy(id -> id, Collectors.counting()));
                topCategoryId = map.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse(null);
            }
        }
        asyncRecordHistory(uid, trimKeyword, topCategoryId);
        return result;
    }

    @Override
    public List<SearchHotVO> getHotSearch(int k) {
        String aggKey = searchProperties.getHot().getAggKey();
        Set<ZSetOperations.TypedTuple<String>> result = stringRedisTemplate
                .opsForZSet().reverseRangeWithScores(aggKey, 0, k - 1);
        if (CollUtil.isEmpty(result)) {
            return Collections.emptyList();
        }
        AtomicInteger index = new AtomicInteger(1);
        return result.stream()
                .map(search -> {
                    SearchHotVO vo = new SearchHotVO();
                    vo.setContent(search.getValue());
                    vo.setId(index.getAndIncrement());
                    return vo;
                }).toList();
    }

    @Override
    public List<SearchRecommendVO> getRecommend(int num) {
        Long uid = StpUtil.getLoginIdAsLong();
        String globalHotKey = searchProperties.getHot().getAggKey();
        String interestKey = searchProperties.getHistory().getInterestPrefix() + uid;

        ArrayList<SearchRecommendVO> result = new ArrayList<>();
        Set<String> selectedWords = new HashSet<>();
        Set<String> topCategories = stringRedisTemplate.opsForZSet().reverseRange(interestKey, 0, 0);
        String favoriteCategoryId = CollUtil.getFirst(topCategories);
        AtomicInteger index = new AtomicInteger(1);
        if (favoriteCategoryId != null) {
            String categoryHotPrefix = searchProperties.getHot().getCategoryHotPrefix() + favoriteCategoryId;
            Set<String> catHotWords = stringRedisTemplate.opsForZSet().reverseRange(categoryHotPrefix, 0, 4);
            if (CollUtil.isNotEmpty(catHotWords)) {
                ArrayList<String> list = new ArrayList<>(catHotWords);
                Collections.shuffle(list);
                list.stream().limit(2)
                        .forEach(word -> {
                            SearchRecommendVO vo = new SearchRecommendVO();
                            vo.setContent(word);
                            vo.setId(index.getAndIncrement());
                            vo.setIsMostRecommended(true);
                            result.add(vo);
                            selectedWords.add(word);
                        });
            }
        }
        int remain = num - result.size();
        Set<String> globalHotWords = stringRedisTemplate.opsForZSet().reverseRange(globalHotKey, 0, 19);
        if (CollUtil.isNotEmpty(globalHotWords)) {
            List<String> remainPool = globalHotWords.stream()
                    .filter(word -> !selectedWords.contains(word))
                    .collect(Collectors.toList());

            Collections.shuffle(remainPool); // 随机打乱
            remainPool.stream().limit(remain).forEach(word -> {
                result.add(new SearchRecommendVO(index.getAndIncrement(), word, false));
            });
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String applyAnchor(AnchorApplyDTO anchorApplyDTO) {
        if (!idCardUtil.isValidIdCard(anchorApplyDTO.getIdCard())) {
            throw new ApiException("身份证号无效");
        }
        long uid = StpUtil.getLoginIdAsLong();
        long id = idGeneratorProvider.getRequired("anchor-apply").generate();
        AnchorApply apply = BeanUtil.copyProperties(anchorApplyDTO, AnchorApply.class);
        apply.setId(id);
        apply.setUid(uid);
        // 这里暂且直接审核通过，实际应用中应管理员进行审核
        apply.setAuditRemark("审核通过");
        apply.setStatus(1);
        boolean saved = anchorApplyService.save(apply);
        if (!saved) {
            throw new ApiException("申请提交失败");
        }

        boolean updated = lambdaUpdate()
                .eq(SysUser::getUid, uid)
                .set(SysUser::getRole, 1)
                .set(SysUser::getDefaultCategoryId, anchorApplyDTO.getCategoryId())
                .update();
        if (!updated) {
            throw new ApiException("更新用户信息失败");
        }

        LiveRoomDTO liveRoomDTO = new LiveRoomDTO();
        liveRoomDTO.setCategoryId(anchorApplyDTO.getCategoryId());

        // 更新用户role与perms
        setRoleAndPerms(getById(uid));
        Result<String> result = liveFeignClient.createRoom(liveRoomDTO);
        if (result.getCode() != 200) {
            throw new ApiException(result.getMsg());
        }
        return result.getData();
    }


    @Async
    public void asyncRecordHistory(Long uid, String keyword, Integer categoryId) {
        String historyKey = searchProperties.getHistory().getPrefix() + uid;
        String rankKey = searchProperties.getHot().getRankKey();
        String hour = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHH"));
        String hourKey = searchProperties.getHot().getHourRankPrefix() + hour;
        stringRedisTemplate.opsForZSet().add(historyKey, keyword, System.currentTimeMillis());
        stringRedisTemplate.opsForZSet()
                .removeRange(historyKey, 0, -searchProperties.getHistory().getMaxCount() - 1);
        stringRedisTemplate.expire(historyKey, searchProperties.getHistory().getTtl());

        stringRedisTemplate.opsForZSet().incrementScore(rankKey, keyword, 1);
        interestUtil.recordSearch(keyword, BehaviorType.SEARCH);

        if (categoryId != null) {
            // 维护用户个人的兴趣偏好 (ZSet)
            interestUtil.recordUserInterest(uid, categoryId, BehaviorType.SEARCH);
        }

        SearchHistory history = new SearchHistory();
        history.setUserId(uid);
        history.setKeyword(keyword);
        history.setCategoryId(categoryId);
        history.setId(idGeneratorProvider.getRequired("search-history").generate());
        searchHistoryService.save(history);
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
                .rank("M".equals(menu.getMenuType()) ? menu.getSort() : null)
                .auths(auths.isEmpty() ? null : auths)
                .showLink(menu.getIsVisible() != null && menu.getIsVisible() == 1)
                .keepAlive(menu.getIsCache() != null && menu.getIsCache() == 1)
                .build();
    }

    // 获取用户关注列表中正在直播的人数
    private Integer getFollowingLiveCount(Long userId) {
        // 获取用户关注列表
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getUserId, userId)
                .eq(UserFollow::getStatus, 1);
        List<UserFollow> followList = userFollowMapper.selectList(wrapper);
        if (CollUtil.isEmpty(followList)) {
            return 0;
        }
        List<Object> ids = followList.stream()
                .map(user -> String.valueOf(user.getFollowUserId()))
                .collect(Collectors.toList());
        List<Object> values = stringRedisTemplate
                .opsForHash().multiGet(anchorMap, ids);
        values = values.stream().filter(Objects::nonNull).collect(Collectors.toList());
        return values.size();
    }

    private void setRoleAndPerms(SysUser user) {
        UserVO userVO = getUserById(user.getUid());
        List<String> roles = new ArrayList<>(userVO.getRoles().stream().map(SysRole::getRoleKey).toList());
        roles.add(user.getRole().name());
        String join = String.join(",", roles);
        StpUtil.getSession().set("role", join);
        List<Long> roleIds = userVO.getRoles().stream().map(SysRole::getRoleId).toList();
        List<SysMenu> menus = roleService.getRolesMenus(roleIds);
        List<String> perms = menus.stream()
                .map(menu -> StrUtil.isNotBlank(menu.getPerms()) ? menu.getPerms() : null)
                .filter(Objects::nonNull).toList();
        String permsJoin = String.join(",", perms);
        StpUtil.getSession().set("perms", permsJoin);
        StpUtil.getSession().set("username", user.getUsername());
        StpUtil.getSession().set("avatar", user.getAvatar() != null ? user.getAvatar() : "");
    }
}




