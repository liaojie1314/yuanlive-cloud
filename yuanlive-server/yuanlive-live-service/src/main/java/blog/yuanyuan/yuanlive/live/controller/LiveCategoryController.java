package blog.yuanyuan.yuanlive.live.controller;

import blog.yuanyuan.yuanlive.common.result.Result;
import blog.yuanyuan.yuanlive.common.result.ResultPage;
import blog.yuanyuan.yuanlive.live.domain.dto.LiveCategoryDTO;
import blog.yuanyuan.yuanlive.live.domain.dto.LiveCategoryQueryDTO;
import blog.yuanyuan.yuanlive.live.domain.vo.LiveCategoryVO;
import blog.yuanyuan.yuanlive.live.domain.vo.LiveRoomRankVO;
import blog.yuanyuan.yuanlive.live.service.LiveCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Update;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/category")
@Tag(name = "直播类别管理")
public class LiveCategoryController {
    @Resource
    private LiveCategoryService categoryService;

    @PostMapping("/add")
    @Operation(summary = "新增分类")
    public Result<String> add(@RequestBody @Validated LiveCategoryDTO categoryDTO) {
        if (categoryService.saveCategory(categoryDTO)) {
            return Result.success(null, "新增成功");
        }
        return Result.failed("新增失败");
    }

    @PostMapping("/update")
    @Operation(summary = "修改分类")
    public Result<String> update(@RequestBody @Validated(Update.class) LiveCategoryDTO categoryDTO) {
        if (categoryService.updateCategory(categoryDTO)) {
            return Result.success(null, "修改成功");
        }
        return Result.failed("修改失败");
    }

    @DeleteMapping("/delete/{ids}")
    @Operation(summary = "批量删除分类")
    public Result<String> remove(@PathVariable("ids") List<Integer> ids) {
        if (categoryService.deleteCategories(ids)) {
            return Result.success(null, "删除成功");
        }
        return Result.failed("删除失败");
    }

    @GetMapping("/getInfo/{id}")
    @Operation(summary = "获取分类详情")
    public Result<LiveCategoryVO> getInfo(@PathVariable("id") Integer id) {
        return Result.success(categoryService.getCategoryById(id));
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询分类列表")
    public Result<ResultPage<LiveCategoryVO>> list(@ParameterObject LiveCategoryQueryDTO queryDTO) {
        return Result.success(categoryService.pageCategories(queryDTO));
    }

    @GetMapping("/tree")
    @Operation(summary = "获取分类树形结构")
    public Result<List<LiveCategoryVO>> tree() {
        return Result.success(categoryService.treeList());
    }

    @GetMapping("/firstLevel")
    @Operation(summary = "获取所有一级分类")
    public Result<List<LiveCategoryVO>> firstLevel() {
        return Result.success(categoryService.getFirstLevelCategories());
    }

    @GetMapping("/listByCategory")
    @Operation(summary = "获取当前直播分类类别的所有直播")
    public Result<List<LiveRoomRankVO>> listByCategory(@RequestParam(value = "categoryValue", required = false) String categoryValue) {
        return Result.success(categoryService.getLiveRoomsByCategoryValue(categoryValue));
    }
}
