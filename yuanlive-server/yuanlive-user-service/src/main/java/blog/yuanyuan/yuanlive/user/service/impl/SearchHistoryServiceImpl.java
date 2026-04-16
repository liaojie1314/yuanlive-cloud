package blog.yuanyuan.yuanlive.user.service.impl;

import blog.yuanyuan.yuanlive.user.domain.dto.KeywordCategoryDTO;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import blog.yuanyuan.yuanlive.entity.user.entity.SearchHistory;
import blog.yuanyuan.yuanlive.user.service.SearchHistoryService;
import blog.yuanyuan.yuanlive.user.mapper.SearchHistoryMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
* @author frodepu
* @description 针对表【search_history(用户搜索记录表)】的数据库操作Service实现
* @createDate 2026-03-05 13:43:44
*/
@Service
public class SearchHistoryServiceImpl extends ServiceImpl<SearchHistoryMapper, SearchHistory>
    implements SearchHistoryService{
    @Resource
    private SearchHistoryMapper searchHistoryMapper;

    @Override
    public List<Integer> getTopCategoriesByUid(Long uid, int i) {
        List<SearchHistory> histories = lambdaQuery()
                .select(SearchHistory::getCategoryId)
                .eq(SearchHistory::getUserId, uid)
                .list();
        Map<Integer, Long> map = histories.stream()
                .map(SearchHistory::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Integer::intValue, Collectors.counting()));
        return map.entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(i)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Integer> getMostFrequentCategories(List<String> hot) {
        if (CollUtil.isEmpty(hot)) return Collections.emptyMap();

        // 1. 从 Mapper 获取聚合后的 DTO 列表
        List<KeywordCategoryDTO> rawData = searchHistoryMapper.selectTopCategoryForKeywords(hot);

        // 2. 将 List 转为 Map
        return rawData.stream()
                .filter(dto -> dto.getCategoryId() != null)
                .collect(Collectors.toMap(
                KeywordCategoryDTO::getKeyword,
                KeywordCategoryDTO::getCategoryId,
                (existing, replacement) -> existing
        ));
    }
}




