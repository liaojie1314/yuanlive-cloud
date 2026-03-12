package blog.yuanyuan.yuanlive.user.job;

import blog.yuanyuan.yuanlive.common.properties.SearchProperties;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.zset.Aggregate;
import org.springframework.data.redis.connection.zset.Weights;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @description: redis中的用户兴趣衰减任务
 * 设置为在每天凌晨 2 点执行
 */

@Component
public class InterestDecayJobHandler {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private SearchProperties searchProperties;

    @XxlJob("decayUserInterestJob")
    public void decayUserInterestJob() {
        String pattern = searchProperties.getHistory().getInterestPrefix() + "*";
        XxlJobHelper.log("开始扫描兴趣 Key 并执行分值衰减...");

        // 1. 使用 SCAN 避免阻塞 Redis 线程
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(1000).build();

        try (Cursor<String> cursor = stringRedisTemplate.scan(options)) {
            List<String> batchKeys = new ArrayList<>();

            while (cursor.hasNext()) {
                batchKeys.add(cursor.next());

                // 每 500 个用户执行一次 Pipeline
                if (batchKeys.size() >= 500) {
                    executeBatchDecay(batchKeys);
                    batchKeys.clear();
                }
            }

            // 处理尾部余量
            if (!batchKeys.isEmpty()) {
                executeBatchDecay(batchKeys);
            }
        }

        XxlJobHelper.log("全量兴趣衰减任务执行成功");
    }

    /**
     * 使用 Pipeline 执行批量衰减
     */
    private void executeBatchDecay(List<String> keys) {
        stringRedisTemplate.executePipelined(new SessionCallback<Object>() {
            @Override
            public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                for (String key : keys) {
                    // 核心黑科技：ZUNIONSTORE key 1 key WEIGHTS 0.9
                    // 参数1: 目标Key, 参数2: 参与合并的Key列表, 参数3: 各Key的权重
                    operations.opsForZSet().unionAndStore(
                            (K) key,
                            Collections.emptyList(), // 只有自己，不传其他 key
                            (K) key,
                            Aggregate.SUM,
                            Weights.of(0.9)
                    );

                    // 清理一下分数过低的“僵尸兴趣”，防止 ZSet 无限膨胀
                    // 删除分数在 [0, 0.1] 之间的成员
                    operations.opsForZSet().removeRangeByScore((K) key, 0, 0.1);
                }
                return null;
            }
        });
    }
}
