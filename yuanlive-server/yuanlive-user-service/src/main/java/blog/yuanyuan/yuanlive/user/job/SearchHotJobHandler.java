package blog.yuanyuan.yuanlive.user.job;

import blog.yuanyuan.yuanlive.user.properties.SearchProperties;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class SearchHotJobHandler {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private SearchProperties searchProperties;

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
        XxlJobHelper.log("48小时聚合操作完成,目标{},影响行数：{}", aggKey, count);
    }
}
