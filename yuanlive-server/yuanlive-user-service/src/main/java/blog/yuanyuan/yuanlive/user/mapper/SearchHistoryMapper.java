package blog.yuanyuan.yuanlive.user.mapper;

import blog.yuanyuan.yuanlive.entity.user.entity.SearchHistory;
import blog.yuanyuan.yuanlive.user.domain.dto.KeywordCategoryDTO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
* @author frodepu
* @description 针对表【search_history(用户搜索记录表)】的数据库操作Mapper
* @createDate 2026-03-05 13:43:44
* @Entity blog.yuanyuan.yuanlive.user.entity.SearchHistory
*/
public interface SearchHistoryMapper extends BaseMapper<SearchHistory> {

    List<KeywordCategoryDTO> selectTopCategoryForKeywords(@Param("hot") List<String> hot);
}




