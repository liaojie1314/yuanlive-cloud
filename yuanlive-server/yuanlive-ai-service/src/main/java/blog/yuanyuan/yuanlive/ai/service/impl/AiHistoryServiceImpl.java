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
        String cursor = dto.getCursor();
        int pageSize = dto.getPageSize();

        // 1. 查询会话获取chatHistory
        MatchOperation match = Aggregation.match(
                Criteria.where("conversationId").is(conversationId).and("uid").is(uid)
        );

        ProjectionOperation project = Aggregation.project()
                .and("checkpoint_content").arrayElementAt(-1).as("lastSnapshot");

        ProjectionOperation finalProject = Aggregation.project()
                .and("lastSnapshot.chatHistory").as("messages");

        Aggregation aggregation = Aggregation.newAggregation(match, project, finalProject);

        Document result = mongoTemplate.aggregate(aggregation, "checkpoint_collection", Document.class)
                .getUniqueMappedResult();

        if (result == null || !result.containsKey("messages")) {
            return ChatHistoryResponseVO.builder()
                    .hasMore(false)
                    .nextCursor(null)
                    .messages(Collections.emptyList())
                    .build();
        }

        List<Document> allMessageDocs = result.getList("messages", Document.class);
        if (allMessageDocs == null || allMessageDocs.isEmpty()) {
            return ChatHistoryResponseVO.builder()
                    .hasMore(false)
                    .nextCursor(null)
                    .messages(Collections.emptyList())
                    .build();
        }

        // 2. 按时间倒序排列(最新的在前面)
        allMessageDocs.sort((a, b) -> {
            Date timeA = a.getDate("timestamp");
            Date timeB = b.getDate("timestamp");
            if (timeA == null && timeB == null) return 0;
            if (timeA == null) return 1;
            if (timeB == null) return -1;
            return timeB.compareTo(timeA);
        });

        // 3. 如果有cursor,找到cursor位置,只取更老的消息
        int startIndex = 0;
        if (cursor != null && !cursor.isEmpty()) {
            for (int i = 0; i < allMessageDocs.size(); i++) {
                // cursor是userId,获取userId进行匹配
                String userId = allMessageDocs.get(i).getString("userId");

                if (cursor.equals(userId)) {
                    startIndex = i + 1;
                    break;
                }
            }
        }

        // 4. 截取需要的消息(从startIndex开始取pageSize条)
        List<Document> pageDocs;
        boolean hasMore;
        if (startIndex >= allMessageDocs.size()) {
            pageDocs = Collections.emptyList();
            hasMore = false;
        } else {
            int endIndex = Math.min(startIndex + pageSize, allMessageDocs.size());
            pageDocs = allMessageDocs.subList(startIndex, endIndex);
            hasMore = endIndex < allMessageDocs.size();
        }

        // 5. 转换为VO
        List<ChatMessageVO> messages = pageDocs.stream()
                .map(this::mapToVO)
                .collect(Collectors.toList());

        // 确定nextCursor(本次返回数据中最老的一条消息ID)
        String nextCursor = null;
        if (!messages.isEmpty() && hasMore) {
            ChatMessageVO oldestMessage = messages.get(messages.size() - 1);
            nextCursor = oldestMessage.getUserId();
        }

        return ChatHistoryResponseVO.builder()
                .hasMore(hasMore)
                .nextCursor(nextCursor)
                .messages(messages)
                .build();
    }

    private ChatMessageVO mapToVO(Document doc) {
        // 获取clientId作为消息ID
        String clientId = null;
        Document metadata = doc.get("metadata", Document.class);
        if (metadata != null) {
            clientId = metadata.getString("clientId");
        }
        if (clientId == null) {
            clientId = doc.getString("clientId");
        }

        // 获取userId
        String userId = null;
        if (metadata != null) {
            userId = metadata.getString("userId");
        }
        if (userId == null) {
            userId = doc.getString("userId");
        }

        // 获取aiId
        String aiId = null;
        if (metadata != null) {
            aiId = metadata.getString("aiId");
        }
        if (aiId == null) {
            aiId = doc.getString("aiId");
        }

        // 获取角色并转换为用户友好的格式
        String role = doc.getString("role");
        String normalizedRole = normalizeRole(role);

        // 获取发送者
        String sender = determineSender(doc, normalizedRole, userId, aiId);

        // 获取头像
        String avatar = determineAvatar(doc, normalizedRole);


        return ChatMessageVO.builder()
                .clientId(clientId)
                .userId(userId)
                .aiId(aiId)
                .role(normalizedRole)
                .sender(sender)
                .avatar(avatar)
                .content(doc.get("content"))
                .time(doc.getDate("timestamp"))
                .thinking(doc.getString("thinking"))
                .build();
    }

    /**
     * 标准化角色名称
     */
    private String normalizeRole(String role) {
        if (role == null) return "user";
        switch (role.toUpperCase()) {
            case "USER":
                return "user";
            case "ASSISTANT":
                return "assistant";
            case "SYSTEM":
                return "system";
            default:
                return role.toLowerCase();
        }
    }

    /**
     * 确定发送者名称
     */
    private String determineSender(Document doc, String role, String userId, String aiId) {
        if ("user".equals(role)) {
            // 用户消息,可以使用userId或查询用户名
            return userId != null ? userId : "user";
        } else if ("assistant".equals(role)) {
            // AI消息,可以使用aiId
            return aiId != null ? aiId : "AI";
        }
        return "system";
    }

    /**
     * 确定头像URL
     */
    private String determineAvatar(Document doc, String role) {
        if ("user".equals(role)) {
            // 用户头像,可以从metadata或单独的用户服务获取
            Document metadata = doc.get("metadata", Document.class);
            if (metadata != null && metadata.containsKey("userAvatar")) {
                return metadata.getString("userAvatar");
            }
            return null; // 或使用默认用户头像
        } else if ("assistant".equals(role)) {
            // AI头像
            Document metadata = doc.get("metadata", Document.class);
            if (metadata != null && metadata.containsKey("aiAvatar")) {
                return metadata.getString("aiAvatar");
            }
            return null; // 或使用默认AI头像
        }
        return null;
    }
}
