package blog.yuanyuan.yuanlive.live.service.impl;

import blog.yuanyuan.yuanlive.common.exception.ApiException;
import blog.yuanyuan.yuanlive.common.result.ResultPage;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import blog.yuanyuan.yuanlive.entity.live.entity.LiveCategory;
import blog.yuanyuan.yuanlive.live.domain.dto.LiveCategoryDTO;
import blog.yuanyuan.yuanlive.live.domain.dto.LiveCategoryQueryDTO;
import blog.yuanyuan.yuanlive.live.domain.vo.LiveCategoryVO;
import blog.yuanyuan.yuanlive.live.service.LiveCategoryService;
import blog.yuanyuan.yuanlive.live.mapper.LiveCategoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author frodepu
* @description 针对表【live_category(直播分类表)】的数据库操作Service实现
* @createDate 2026-01-27 14:55:18
*/
@Slf4j
@Service
public class LiveCategoryServiceImpl extends ServiceImpl<LiveCategoryMapper, LiveCategory>
    implements LiveCategoryService{

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveCategory(LiveCategoryDTO categoryDTO) {
        // 检查同级别下是否已存在同名分类
        LambdaQueryWrapper<LiveCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LiveCategory::getName, categoryDTO.getName());
        if (categoryDTO.getParentId() != null) {
            queryWrapper.eq(LiveCategory::getParentId, categoryDTO.getParentId());
        } else {
            queryWrapper.eq(LiveCategory::getParentId, 0);
        }

        if (this.count(queryWrapper) > 0) {
            log.warn("分类名称已存在: {}", categoryDTO.getName());
            throw new ApiException("同级别下已存在该分类名称");
        }

        // 如果有父分类，检查父分类是否存在
        if (categoryDTO.getParentId() != null && categoryDTO.getParentId() > 0) {
            LiveCategory parent = this.getById(categoryDTO.getParentId());
            if (parent == null) {
                log.warn("父分类不存在, parentId: {}", categoryDTO.getParentId());
                throw new ApiException("父分类不存在");
            }
        }

        LiveCategory category = new LiveCategory();
        BeanUtil.copyProperties(categoryDTO, category);
        
        // 如果没有设置父ID，默认为0（一级分类）
        if (category.getParentId() == null) {
            category.setParentId(0);
        }
        
        // 如果没有设置排序权重，默认为0
        if (category.getSortWeight() == null) {
            category.setSortWeight(0);
        }
        
        boolean result = this.save(category);
        log.info("分类新增{}, 分类ID: {}", result ? "成功" : "失败", category.getId());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCategory(LiveCategoryDTO categoryDTO) {
        log.info("开始修改分类, 分类ID: {}", categoryDTO.getId());
        
        if (categoryDTO.getId() == null) {
            log.warn("修改分类时分类ID不能为空");
            throw new ApiException("分类ID不能为空");
        }
        
        // 检查分类是否存在
        LiveCategory existCategory = this.getById(categoryDTO.getId());
        if (existCategory == null) {
            log.warn("分类不存在, 分类ID: {}", categoryDTO.getId());
            throw new ApiException("分类不存在");
        }

        // 检查同级别下是否已存在同名分类（排除自身）
        if (StrUtil.isNotBlank(categoryDTO.getName())) {
            LambdaQueryWrapper<LiveCategory> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(LiveCategory::getName, categoryDTO.getName())
                    .ne(LiveCategory::getId, categoryDTO.getId());
            
            Integer parentId = categoryDTO.getParentId() != null ? categoryDTO.getParentId() : existCategory.getParentId();
            queryWrapper.eq(LiveCategory::getParentId, parentId);

            if (this.count(queryWrapper) > 0) {
                log.warn("分类名称已存在: {}", categoryDTO.getName());
                throw new ApiException("同级别下已存在该分类名称");
            }
        }
        
        // 如果修改了父分类，检查父分类是否存在且不能为自身
        if (categoryDTO.getParentId() != null) {
            if (categoryDTO.getParentId().equals(categoryDTO.getId())) {
                log.warn("父分类不能为自身, 分类ID: {}", categoryDTO.getId());
                throw new ApiException("父分类不能为自身");
            }
            
            if (categoryDTO.getParentId() > 0) {
                LiveCategory parent = this.getById(categoryDTO.getParentId());
                if (parent == null) {
                    log.warn("父分类不存在, parentId: {}", categoryDTO.getParentId());
                    throw new ApiException("父分类不存在");
                }
            }
        }
        
        LiveCategory category = new LiveCategory();
        BeanUtil.copyProperties(categoryDTO, category);
        
        boolean result = this.updateById(category);
        log.info("分类修改{}, 分类ID: {}", result ? "成功" : "失败", category.getId());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCategories(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            log.warn("删除分类IDs为空");
            throw new ApiException("请选择要删除的分类");
        }
        // 检查要删除的分类是否有子分类
        LambdaQueryWrapper<LiveCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(LiveCategory::getParentId, ids);
        long childCount = this.count(queryWrapper);
        if (childCount > 0) {
            log.warn("存在子分类，不能删除");
            throw new ApiException("该分类下存在子分类，请先删除子分类");
        }
        // TODO: 检查是否有直播间使用该分类
        
        boolean result = this.removeByIds(ids);
        log.info("分类删除{}, 删除数量: {}", result ? "成功" : "失败", ids.size());
        return result;
    }

    @Override
    public LiveCategoryVO getCategoryById(Integer id) {
        if (id == null) {
            log.warn("分类ID不能为空");
            throw new ApiException("分类ID不能为空");
        }
        
        LiveCategory category = this.getById(id);
        if (category == null) {
            log.warn("分类不存在, 分类ID: {}", id);
            throw new ApiException("分类不存在");
        }

        return BeanUtil.copyProperties(category, LiveCategoryVO.class);
    }

    @Override
    public ResultPage<LiveCategoryVO> pageCategories(LiveCategoryQueryDTO queryDTO) {
        Page<LiveCategory> page = lambdaQuery()
                .eq(queryDTO.getParentId() != null, LiveCategory::getParentId, queryDTO.getParentId())
                .like(StrUtil.isNotBlank(queryDTO.getName()), LiveCategory::getName, queryDTO.getName())
                .orderByDesc(LiveCategory::getSortWeight)
                .orderByAsc(LiveCategory::getId)
                .page(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()));

        List<LiveCategoryVO> voList = page.getRecords().stream()
                .map(category -> BeanUtil.copyProperties(category, LiveCategoryVO.class))
                .collect(Collectors.toList());
        Page<LiveCategoryVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(voList);
        return ResultPage.of(voPage);
    }

    @Override
    public List<LiveCategoryVO> treeList() {
        // 查询所有分类
        List<LiveCategory> allCategories = this.list(
                new LambdaQueryWrapper<LiveCategory>()
                        .orderByDesc(LiveCategory::getSortWeight)
                        .orderByAsc(LiveCategory::getId)
        );
        
        // 转换为VO
        List<LiveCategoryVO> allVOs = allCategories.stream()
                .map(category -> BeanUtil.copyProperties(category, LiveCategoryVO.class))
                .collect(Collectors.toList());
        
        // 构建树形结构
        return buildTree(allVOs, 0);
    }

    @Override
    public List<LiveCategoryVO> getFirstLevelCategories() {
        log.info("获取一级分类列表");
        
        LambdaQueryWrapper<LiveCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LiveCategory::getParentId, 0)
                .orderByDesc(LiveCategory::getSortWeight)
                .orderByAsc(LiveCategory::getId);
        
        List<LiveCategory> categories = this.list(queryWrapper);
        
        return categories.stream()
                .map(category -> BeanUtil.copyProperties(category, LiveCategoryVO.class))
                .collect(Collectors.toList());
    }

    /**
     * 构建树形结构
     * @param allCategories 所有分类
     * @param parentId 父ID
     * @return 树形结构
     */
    private List<LiveCategoryVO> buildTree(List<LiveCategoryVO> allCategories, Integer parentId) {
        List<LiveCategoryVO> tree = new ArrayList<>();
        
        for (LiveCategoryVO category : allCategories) {
            Integer categoryParentId = category.getParentId() != null ? category.getParentId() : 0;
            
            if (categoryParentId.equals(parentId)) {
                // 递归查找子分类
                List<LiveCategoryVO> children = buildTree(allCategories, category.getId());
                if (!children.isEmpty()) {
                    category.setChildren(children);
                }
                tree.add(category);
            }
        }
        
        return tree;
    }
}




