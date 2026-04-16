package blog.yuanyuan.yuanlive.ai.job;

import blog.yuanyuan.yuanlive.ai.domain.doc.UserQueryDOC;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class AiRecommendJobHandler {
    @Resource
    private ElasticsearchOperations esOperations;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource(name = "openAiEmbeddingModel")
    private EmbeddingModel embeddingModel;

    @Value("${redis-key.ai.search-recommend-key}")
    private String RECOMMEND_KEY;

    @XxlJob("refreshAiRecommendJob")
    public void refreshAiRecommendJob() {
        XxlJobHelper.log("开始执行全平台 AI 推荐词刷新任务...");

        try {
            List<String> results = refreshGlobalRecommendations();

            XxlJobHelper.log("AI 推荐词刷新成功，当前热点内容: " + String.join(" | ", results));
        } catch (Exception e) {
            XxlJobHelper.log("AI 推荐词刷新任务执行异常: " + e.getMessage());
            log.error("Global Recommend Job Error", e);
            // 异常上报给 XXL-JOB 后端
            XxlJobHelper.handleFail("任务执行失败，请检查 AI 模型或 ES 连接");
        }

        XxlJobHelper.log("全平台 AI 推荐词刷新任务执行结束");
    }

    private List<String> refreshGlobalRecommendations() {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.range(r -> r.date(d -> d
                        .field("createTime")
                        .gte("now-1d"))))
                .withAggregation("hot_topics", Aggregation.of(a -> a
                        .significantText(st -> st
                                .field("content")
                                .size(3)
                                .minDocCount(1L)
                                .filterDuplicateText(true))))
                .withAggregation("backup_topics", Aggregation.of(a -> a
                        .terms(t -> t
                                .field("content.keyword")
                                .size(5))))
                .withMaxResults(0)
                .build();

        SearchHits<UserQueryDOC> searchHits = esOperations.search(query, UserQueryDOC.class);
        XxlJobHelper.log("ES 查询命中文档数: " + searchHits.getTotalHits());

        // 解析结果
        Set<String> uniqueQuestions = new LinkedHashSet<>();
        if (searchHits.hasAggregations()) {
            ElasticsearchAggregations aggregations = (ElasticsearchAggregations) searchHits.getAggregations();
            List<String> sigWords = extractKeysFromAgg(aggregations, "hot_topics");

            if (!sigWords.isEmpty()) {
                XxlJobHelper.log("提取到显著性热词，正在进行向量代表作转换...");
                for (String word : sigWords) {
                    XxlJobHelper.log("正在处理显著性热词: {}", word);
                    String representative = findMostRepresentativeContent(word);
                    XxlJobHelper.log("向量代表作: {}", representative);
                    if (StrUtil.isNotBlank(representative)) {
                        uniqueQuestions.add(representative);
                    }
                    if (uniqueQuestions.size() >= 3) break; // 够 3 条就停
                }
            }
            // 兜底策略：如果是 terms 聚合，说明拿到的是完整文本/高频文本，直接使用
            if (uniqueQuestions.size() < 3) {
                XxlJobHelper.log("当前热搜仅 {} 条，尝试使用 Terms 兜底策略填充...", uniqueQuestions.size());
                List<String> backupTexts = extractKeysFromAgg(aggregations, "backup_topics");

                for (String text : backupTexts) {
                    if (StrUtil.isNotBlank(text)) {
                        // LinkedHashSet 会自动处理掉重复的句子
                        uniqueQuestions.add(text);
                    }
                    if (uniqueQuestions.size() >= 3) {
                        XxlJobHelper.log("已填满 3 条热搜。");
                        break;
                    }
                }
            }
        }

        List<String> finalResults = new ArrayList<>(uniqueQuestions);
        if (!finalResults.isEmpty()) {
            stringRedisTemplate.opsForValue().set(RECOMMEND_KEY, JSONUtil.toJsonStr(finalResults));
        }

        return finalResults;
    }

    private String findMostRepresentativeContent(String hotWord) {
        try {
            float[] vector = embeddingModel.embed(hotWord);
            List<Float> list = new ArrayList<>(vector.length);
            for (float f : vector) {
                list.add(f);
            }

            // 2. 构建 k-NN 查询
            // 在 ES 8.x 中，knn 是与 query 平级的顶级参数
            NativeQuery knnQuery = NativeQuery.builder()
                    .withKnnSearches(k -> k
                            .field("contentVector") // 向量字段
                            .queryVector(list)    // 搜索向量
                            .k(1)                   // 只取最相近的一个
                            .numCandidates(10)      // 参与比对的候选数量
                            .filter(f -> f.range(r -> r.date(d -> d
                                    .field("createTime").gte("now-1d"))))
                    )
                    .withPageable(PageRequest.of(0, 1)) // 只需要第1条
                    .build();

            // 3. 执行查询
            SearchHits<UserQueryDOC> hits = esOperations.search(knnQuery, UserQueryDOC.class);

            if (hits.hasSearchHits()) {
                // 返回最相近的那条原始提问
                return hits.getSearchHit(0).getContent().getContent();
            }
        } catch (Exception e) {
            log.error("根据热词获取向量代表作失败: {}", hotWord, e);
        }
        return hotWord; // 如果查询失败，保底返回原词
    }

    // 从不同类型的聚合结果中提取 Key
    private List<String> extractKeysFromAgg(ElasticsearchAggregations aggs, String name) {
        List<String> keys = new ArrayList<>();
        ElasticsearchAggregation aggInstance = aggs.get(name);
        if (aggInstance != null) {
            Aggregate aggregate = aggInstance.aggregation().getAggregate();
            // 处理 Significant Text 结果
            if (aggregate.isSigsterms()) {
                aggregate.sigsterms().buckets().array().forEach(b -> {
                    String key = b.key();
                    if (key != null && key.length() > 1) {
                        keys.add(key);
                    }
                });
            }
            // 处理 Terms 结果
            else if (aggregate.isLterms()) {
                aggregate.lterms().buckets().array().forEach(b -> keys.add(b.keyAsString()));
            }
            else if (aggregate.isSterms()) {
                aggregate.sterms().buckets().array().forEach(b -> keys.add(b.key().stringValue()));
            }
        }
        return keys;
    }
}
