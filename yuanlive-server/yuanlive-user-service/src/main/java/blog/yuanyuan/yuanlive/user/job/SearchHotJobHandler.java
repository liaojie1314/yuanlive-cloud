package blog.yuanyuan.yuanlive.user.job;

import blog.yuanyuan.yuanlive.common.properties.SearchProperties;
import blog.yuanyuan.yuanlive.user.service.SearchHistoryService;
import cn.hutool.core.collection.CollUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SearchHotJobHandler {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private SearchProperties searchProperties;
    @Resource
    private SearchHistoryService searchHistoryService;

    @XxlJob("update48hHotSearchHandler")
    public void update48hHotSearchHandler() {
        log.info("更新48小时热门搜索任务开始执行");
        XxlJobHelper.log("更新48小时热门搜索任务开始执行");
        String hourRankPrefix = searchProperties.getHot().getHourRankPrefix();
        String aggKey = searchProperties.getHot().getAggKey();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHH");
        ArrayList<String> hourKeys = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < 48; i++) {
            hourKeys.add(hourRankPrefix + now.minusHours(i).format(formatter));
        }
        // 执行48小时聚合操作
        String firstKey = hourKeys.get(0);
        List<String> otherKeys = hourKeys.subList(1, hourKeys.size());
        Long count = stringRedisTemplate.opsForZSet().unionAndStore(firstKey, otherKeys, aggKey);
        // 获取 Top 200 热词并分发到分类桶
        List<ZSetOperations.TypedTuple<String>> hotTuples = stringRedisTemplate.opsForZSet()
                .reverseRangeWithScores(aggKey, 0, 199).stream().toList();
        if (CollUtil.isEmpty(hotTuples)) {
            XxlJobHelper.log("未发现热搜词条，跳过分类分发。");
            return;
        }
        List<String> hot = hotTuples.stream()
                .map(ZSetOperations.TypedTuple::getValue)
                .collect(Collectors.toList());

        Map<String, Integer> keywordCategoryMap = searchHistoryService.getMostFrequentCategories(hot);
        stringRedisTemplate.executePipelined(new SessionCallback<Object>() {
            @Override
            public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                // 这些操作会被自动放入 Pipeline 中批量发送
                for (ZSetOperations.TypedTuple<String> tuple : hotTuples) {
                    String word = tuple.getValue();
                    Double score = tuple.getScore();
                    Integer catId = keywordCategoryMap.get(word);
                    if (catId != null) {
                        String catKey = searchProperties.getHot().getCategoryHotPrefix() + catId;
                        operations.opsForZSet().add((K) catKey, (V) word, score);
                    }
                }
                return null;
            }
        });
        XxlJobHelper.log("48小时聚合操作完成,目标{},影响行数：{}", aggKey, count);
    }
}
