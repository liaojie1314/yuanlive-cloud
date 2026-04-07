package blog.yuanyuan.yuanlive.ai.service.impl;

import blog.yuanyuan.yuanlive.ai.domain.dto.HistoryRequestDTO;
import blog.yuanyuan.yuanlive.ai.domain.dto.SessionDeleteDTO;
import blog.yuanyuan.yuanlive.ai.domain.dto.SessionPageQueryDTO;
import blog.yuanyuan.yuanlive.ai.domain.dto.TitleUpdateDTO;
import blog.yuanyuan.yuanlive.ai.domain.vo.AiSessionVO;
import blog.yuanyuan.yuanlive.ai.domain.vo.ChatHistoryResponseVO;
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
        long total = mongoTemplate.count(query, "chat_session");
        query.fields()
                .include("conversationId")
                .include("title")
                .include("lastUpdateTime")
                .include("isTop");
        query.with(Sort.by(Sort.Direction.DESC, "isTop", "lastUpdateTime"));
        query.skip(skip).limit(pageSize);

        List<Document> documents = mongoTemplate
                .find(query, Document.class, "chat_session");
        List<AiSessionVO> collection = documents.stream()
                .map(doc -> {
                    AiSessionVO vo = new AiSessionVO();
                    vo.setTitle(doc.getString("title"));
                    vo.setConversationId(doc.getString("conversationId"));
                    vo.setIsTop(doc.getBoolean("isTop"));
                    Date time = doc.getDate("lastUpdateTime");
                    if (time != null) {
                        vo.setTimestamp(time.getTime() / 1000);
                    }
                    return vo;
                }).toList();

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
        UpdateResult result = mongoTemplate.updateFirst(query, update, "chat_session");
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
        UpdateResult result = mongoTemplate.updateFirst(query, update, "chat_session");

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
        DeleteResult result = mongoTemplate.remove(query, "chat_session");
        mongoTemplate.remove(query, "chat_history");
        mongoTemplate.remove(query, "checkpoint_collection");
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
        DeleteResult result = mongoTemplate.remove(query, "chat_session");
        mongoTemplate.remove(query, "chat_history");
        mongoTemplate.remove(query, "checkpoint_collection");
        if (result.getDeletedCount() > 0) {
            return Result.success("成功删除 " + result.getDeletedCount() + " 条会话");
        } else {
            return Result.failed("未找到可删除的会话");
        }
    }

    @Override
    public ChatHistoryResponseVO getChatHistory(String conversationId, HistoryRequestDTO dto) {
        String uid = StpUtil.getLoginIdAsString();
        String cursor = dto.getCursor(); // 传入的是上一次返回的最老消息的 userId (msgId)
        int pageSize = dto.getPageSize();

        // 1. 构建基础查询条件
        Criteria criteria = Criteria.where("conversationId").is(conversationId).and("uid").is(uid);

        // 2. 处理游标分页逻辑
        if (cursor != null && !cursor.isEmpty()) {
            // 先找到游标对应的那条消息，获取它的时间戳
            Query cursorQuery = new Query(Criteria.where("userId").is(cursor));
            Document cursorDoc = mongoTemplate.findOne(cursorQuery, Document.class, "chat_history");

            if (cursorDoc != null) {
                Date cursorTime = cursorDoc.getDate("time");
                // 只查询时间早于（小于）游标消息的消息
                criteria.and("time").lt(cursorTime);
            }
        }

        // 3. 执行查询：按时间倒序排列
        Query query = new Query(criteria)
                .with(Sort.by(Sort.Direction.DESC, "time"))
                .limit(pageSize + 1); // 多取一条用来判断 hasMore

        List<Document> docs = mongoTemplate.find(query, Document.class, "chat_history");

        // 4. 判断是否还有更多数据
        boolean hasMore = docs.size() > pageSize;
        List<Document> resultDocs = hasMore ? docs.subList(0, pageSize) : docs;

        // 5. 转换为 VO
        List<ChatMessageVO> messages = resultDocs.stream()
                .map(this::mapToVO)
                .collect(Collectors.toList());

        // 6. 确定下一次查询的游标 (本次结果中最老的一条消息的 userId)
        String nextCursor = null;
        if (!messages.isEmpty()) {
            nextCursor = messages.get(messages.size() - 1).getUserId();
        }

        return ChatHistoryResponseVO.builder()
                .hasMore(hasMore)
                .nextCursor(hasMore ? nextCursor : null) // 如果没有更多了，nextCursor 返回空
                .messages(messages)
                .build();
    }

    private ChatMessageVO mapToVO(Document doc) {
        ChatMessageVO vo = ChatMessageVO.builder()
                .userId(doc.getString("userId"))
                .clientId(doc.getString("clientId"))
                .aiId(doc.getString("aiId"))
                .role(doc.getString("role"))
                .content(doc.getString("content"))
                .sender(doc.getString("sender"))
                .thinking(doc.getString("thinking"))
                .build();

        Date date = doc.getDate("time");
        if (date != null) {
            // 返回秒时间戳
            vo.setTime(date.getTime() / 1000);
        }
        if ("user".equals(vo.getRole())) {
            vo.setAvatar(StpUtil.getSession().getString("avatar"));
        }
        return vo;
    }

}
