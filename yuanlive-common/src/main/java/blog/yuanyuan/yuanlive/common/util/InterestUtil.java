package blog.yuanyuan.yuanlive.common.util;

import blog.yuanyuan.yuanlive.common.enums.BehaviorType;
import blog.yuanyuan.yuanlive.common.properties.SearchProperties;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class InterestUtil {
    @Resource
    private SearchProperties searchProperties;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    public void recordUserInterest(Long uid, Integer categoryId, BehaviorType behaviorType) {
        if (uid == null || categoryId == null) return;
        String interestKey = searchProperties.getHistory().getInterestPrefix() + uid;
        // 获取对应行为的权重分
        double weight = behaviorType.getWeight();
        // 累加兴趣分
        stringRedisTemplate.opsForZSet().incrementScore(interestKey, categoryId.toString(), weight);
    }

    public void recordSearch(String keyword, BehaviorType behaviorType) {
        String hour = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHH"));
        String hourKey = searchProperties.getHot().getHourRankPrefix() + hour;
        stringRedisTemplate.opsForZSet()
                .incrementScore(hourKey, keyword, behaviorType.getWeight());
        Long expire = stringRedisTemplate.getExpire(hourKey);
        if (expire == -1) {
            stringRedisTemplate.expire(hourKey, searchProperties.getHot().getTtl());
        }
    }

    public void reduceUserInterest(Long uid, Integer categoryId, BehaviorType type) {
        if (uid == null || categoryId == null) return;
        String interestKey = searchProperties.getHistory().getInterestPrefix() + uid;
        double weight = -type.getWeight();
        stringRedisTemplate.opsForZSet().incrementScore(interestKey, categoryId.toString(), weight);
    }

    public void reduceSearch(String keyword, BehaviorType type) {
        String hour = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHH"));
        String hourKey = searchProperties.getHot().getHourRankPrefix() + hour;
        stringRedisTemplate.opsForZSet()
                .incrementScore(hourKey, keyword, -type.getWeight());
    }
}
