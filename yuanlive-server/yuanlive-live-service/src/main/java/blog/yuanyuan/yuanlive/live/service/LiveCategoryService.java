package blog.yuanyuan.yuanlive.live.service;

import blog.yuanyuan.yuanlive.common.result.ResultPage;
import blog.yuanyuan.yuanlive.entity.live.entity.LiveCategory;
import blog.yuanyuan.yuanlive.live.domain.dto.LiveCategoryDTO;
import blog.yuanyuan.yuanlive.live.domain.dto.LiveCategoryQueryDTO;
import blog.yuanyuan.yuanlive.live.domain.vo.HotCategoryVO;
import blog.yuanyuan.yuanlive.live.domain.vo.LiveCategoryTreeVO;
import blog.yuanyuan.yuanlive.live.domain.vo.LiveCategoryVO;
import blog.yuanyuan.yuanlive.entity.live.vo.LiveRoomRankVO;
import blog.yuanyuan.yuanlive.live.domain.vo.LiveChildVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author frodepu
* @description 针对表【live_category(直播分类表)】的数据库操作Service
* @createDate 2026-01-27 14:55:18
*/
public interface LiveCategoryService extends IService<LiveCategory> {

    /**
     * 新增分类
     * @param categoryDTO 分类信息
     * @return 结果
     */
    boolean saveCategory(LiveCategoryDTO categoryDTO);

    /**
     * 修改分类
     * @param categoryDTO 分类信息
     * @return 结果
     */
    boolean updateCategory(LiveCategoryDTO categoryDTO);

    /**
     * 批量删除分类
     * @param ids 分类ID集合
     * @return 结果
     */
    boolean deleteCategories(List<Integer> ids);

    /**
     * 根据ID获取分类详情
     * @param id 分类ID
     * @return 分类详情
     */
    LiveCategoryVO getCategoryById(Integer id);

    /**
     * 分页查询分类列表
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    ResultPage<LiveCategoryVO> pageCategories(LiveCategoryQueryDTO queryDTO);

    /**
     * 获取分类树形结构
     * @return 分类树
     */
    List<LiveCategoryTreeVO> treeList();

    /**
     * 获取所有一级分类
     * @return 一级分类列表
     */
    List<LiveCategoryVO> getFirstLevelCategories();

    List<LiveRoomRankVO> getLiveRoomsByCategoryValue(String value);

    List<HotCategoryVO> getHotCategory();

    Integer getCategoryIdBySearch(String keyword);

    List<LiveChildVO> getChildren();
}
