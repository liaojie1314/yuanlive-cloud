package blog.yuanyuan.yuanlive.ai.service.impl;

import blog.yuanyuan.yuanlive.ai.domain.dto.SessionDeleteDTO;
import blog.yuanyuan.yuanlive.ai.domain.dto.SessionPageQueryDTO;
import blog.yuanyuan.yuanlive.ai.domain.dto.TitleUpdateDTO;
import blog.yuanyuan.yuanlive.ai.domain.vo.AiSessionVO;
import blog.yuanyuan.yuanlive.ai.domain.vo.ChatMessageVO;
import blog.yuanyuan.yuanlive.ai.domain.vo.PinSessionVO;
import blog.yuanyuan.yuanlive.ai.service.AiHistoryService;
import blog.yuanyuan.yuanlive.common.result.Result;
import blog.yuanyuan.yuanlive.common.result.ResultPage;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import jakarta.annotation.Resource;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AiHistoryServiceImpl implements AiHistoryService {
    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public ResultPage<AiSessionVO> getUserSessionList(SessionPageQueryDTO queryDTO) {
        Integer pageNum = queryDTO.getPageNum();
        Integer pageSize = queryDTO.getPageSize();
        String uid = StpUtil.getLoginIdAsString();
        long skip = (long) (pageNum - 1) * pageSize;

        Query query = new Query(Criteria.where("uid").is(uid));
        long total = mongoTemplate.count(query, "checkpoint_collection");
        query.fields()
                .include("conversationId")
                .include("title")
                .include("lastUpdateTime")
                .include("isTop");
        query.with(Sort.by(Sort.Direction.DESC, "isTop", "lastUpdateTime"));
        query.skip(skip).limit(pageSize);

        List<AiSessionVO> collection = mongoTemplate
                .find(query, AiSessionVO.class, "checkpoint_collection");
        Page<AiSessionVO> page = new Page<>(pageNum, pageSize, total);
        page.setRecords(collection);
        return ResultPage.of(page);
    }

    @Override
    public Result<String> updateTitle(TitleUpdateDTO titleUpdateDTO) {
        String uid = StpUtil.getLoginIdAsString();
        Query query = new Query(Criteria.where("conversationId").is(titleUpdateDTO.getConversationId())
                .and("uid").is(uid));
        Update update = new Update().set("title", titleUpdateDTO.getTitle());
        update.set("lastUpdateTime", new Date());
        UpdateResult result = mongoTemplate.updateFirst(query, update, "checkpoint_collection");
        if (result.getMatchedCount() > 0) {
            return Result.success("标题修改成功");
        } else {
            return Result.failed("标题修改失败, 会话不存在或无权操作");
        }
    }

    @Override
    public Result<PinSessionVO> pinSession(String conversationId) {
        return updateIsTop(conversationId, true);
    }

    @Override
    public Result<PinSessionVO> unpinSession(String conversationId) {
        return updateIsTop(conversationId, false);
    }

    private Result<PinSessionVO> updateIsTop(String conversationId, boolean isTop) {
        String uid = StpUtil.getLoginIdAsString();
        Query query = new Query(Criteria.where("conversationId").is(conversationId)
                .and("uid").is(uid));
        Update update = new Update().set("isTop", isTop);
        UpdateResult result = mongoTemplate.updateFirst(query, update, "checkpoint_collection");

        if (result.getMatchedCount() > 0) {
            return Result.success(new PinSessionVO(isTop));
        } else {
            return Result.failed("会话不存在或无权操作");
        }
    }

    @Override
    public Result<String> deleteSessions(SessionDeleteDTO deleteDTO) {
        String uid = StpUtil.getLoginIdAsString();
        List<String> conversationIds = deleteDTO.getConversationIds();
        Query query = new Query(
                Criteria.where("conversationId").in(conversationIds)
                        .and("uid").is(uid)
        );
        DeleteResult result = mongoTemplate.remove(query, "checkpoint_collection");
        long deletedCount = result.getDeletedCount();
        if (deletedCount > 0) {
            return Result.success("成功删除 " + deletedCount + " 条会话");
        } else {
            return Result.failed("未找到可删除的会话或无权操作");
        }
    }

    @Override
    public Result<String> deleteAllSessions() {
        String uid = StpUtil.getLoginIdAsString();
        Query query = new Query(Criteria.where("uid").is(uid));
        DeleteResult result = mongoTemplate.remove(query, "checkpoint_collection");
        if (result.getDeletedCount() > 0) {
            return Result.success("成功删除所有会话");
        } else {
            return Result.failed("未找到可删除的会话");
        }
    }

    @Override
    public List<ChatMessageVO> getChatHistory(String conversationId) {
        String uid = StpUtil.getLoginIdAsString();

        MatchOperation match = Aggregation.match(
                Criteria.where("conversationId").is(conversationId).and("uid").is(uid)
        );

        // 2. 提取最后一个快照中的 chatHistory
        // 逻辑：checkpoint_content 的最后一位 -> 取其 chatHistory 字段
        ProjectionOperation project = Aggregation.project()
                .and("checkpoint_content").arrayElementAt(-1).as("lastSnapshot");

        ProjectionOperation finalProject = Aggregation.project()
                .and("lastSnapshot.chatHistory").as("messages");

        // 3. 执行聚合
        Aggregation aggregation = Aggregation.newAggregation(match, project, finalProject);

        Document result = mongoTemplate.aggregate(aggregation, "checkpoint_collection", Document.class)
                .getUniqueMappedResult();

        if (result == null || !result.containsKey("messages")) {
            return Collections.emptyList();
        }

        // 4. 将 Document 列表映射为 VO 列表
        List<Document> messageDocs = result.getList("messages", Document.class);
        return messageDocs.stream().map(this::mapToVO).collect(Collectors.toList());
    }

    private ChatMessageVO mapToVO(Document doc) {
        return ChatMessageVO.builder()
                .role(doc.getString("role"))
                .content(doc.get("content"))
                .thinking(doc.getString("thinking"))
                .timestamp(doc.getDate("timestamp"))
                .metadata(doc.get("metadata", Map.class))
                .build();
    }
}
