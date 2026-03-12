package blog.yuanyuan.yuanlive.user.service;

import blog.yuanyuan.yuanlive.entity.user.entity.SearchHistory;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
* @author frodepu
* @description 针对表【search_history(用户搜索记录表)】的数据库操作Service
* @createDate 2026-03-05 13:43:44
*/
public interface SearchHistoryService extends IService<SearchHistory> {

    List<Integer> getTopCategoriesByUid(Long uid, int i);

    Map<String, Integer> getMostFrequentCategories(List<String> hot);
}
