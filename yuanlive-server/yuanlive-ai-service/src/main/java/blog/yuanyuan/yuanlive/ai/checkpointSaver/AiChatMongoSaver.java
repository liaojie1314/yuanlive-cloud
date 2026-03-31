package blog.yuanyuan.yuanlive.ai.checkpointSaver;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.checkpoint.BaseCheckpointSaver;
import com.alibaba.cloud.ai.graph.checkpoint.Checkpoint;
import com.alibaba.cloud.ai.graph.checkpoint.savers.mongo.MongoSaver;
import com.alibaba.cloud.ai.graph.serializer.Serializer;
import com.alibaba.cloud.ai.graph.serializer.StateSerializer;
import com.alibaba.cloud.ai.graph.serializer.check_point.CheckPointSerializer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.ClientSessionOptions;
import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.*;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;


public class AiChatMongoSaver implements BaseCheckpointSaver {
    private static final Logger logger = LoggerFactory.getLogger(MongoSaver.class);
    private static final String DB_NAME = "check_point_db";
    private static final String THREAD_META_COLLECTION = "thread_meta";
    private static final String CHECKPOINT_COLLECTION = "checkpoint_collection";
    private static final String THREAD_META_PREFIX = "mongo:thread:meta:";
    private static final String CHECKPOINT_PREFIX = "mongo:checkpoint:content:";
    private static final String DOCUMENT_CONTENT_KEY = "checkpoint_content";
    // Thread meta document field names
    private static final String FIELD_THREAD_ID = "thread_id";
    private static final String FIELD_IS_RELEASED = "is_released";
    private static final String FIELD_THREAD_NAME = "thread_name";
    private final Serializer<Checkpoint> checkpointSerializer;
    private MongoClient client;
    private MongoDatabase database;
    private TransactionOptions txnOptions;
    private static final ObjectMapper jsonMapper = new ObjectMapper();

    /**
     * Protected constructor for MongoSaver.
     * Use {@link #builder()} to create instances.
     *
     * @param client          the client
     * @param stateSerializer the state serializer
     */
    protected AiChatMongoSaver(MongoClient client, StateSerializer stateSerializer) {
        Objects.requireNonNull(client, "client cannot be null");
        Objects.requireNonNull(stateSerializer, "stateSerializer cannot be null");
        this.client = client;
        this.database = client.getDatabase(DB_NAME);
        this.txnOptions = TransactionOptions.builder().writeConcern(WriteConcern.MAJORITY).build();
        this.checkpointSerializer = new CheckPointSerializer(stateSerializer);
        Runtime.getRuntime().addShutdownHook(new Thread(client::close));
    }

    /**
     * Creates a new builder for MongoSaver.
     *
     * @return a new Builder instance
     */
    public static AiChatMongoSaver.Builder builder() {
        return new AiChatMongoSaver.Builder();
    }

    private List<Document> convertToBsonList(List<Checkpoint> checkpoints, RunnableConfig config) {
        List<Document> list = new ArrayList<>();
        Map<String, Object> runMeta = config.metadata().orElse(Collections.emptyMap());

        for (Checkpoint cp : checkpoints) {
            Document cpDoc = new Document()
                    .append("id", cp.getId())
                    .append("nodeId", cp.getNodeId())
                    .append("nextNodeId", cp.getNextNodeId());

            // 提取消息并平铺字段
            List<Document> msgDocs = new ArrayList<>();
            Object messages = cp.getState().get("messages");
            if (messages instanceof List<?> msgList) {
                for (Object obj : msgList) {
                    if (obj instanceof Message msg) {
                        Document m = new Document();
                        m.append("role", msg.getMessageType().name());

                        // 获取内容：如果是工具消息，提取 responseData
                        String rawText = msg.getText();
                        if (msg instanceof ToolResponseMessage trm && !trm.getResponses().isEmpty()) {
                            rawText = trm.getResponses().get(0).responseData();
                        }
                        m.append("content", parseToBson(rawText));

                        // 注入业务 ID 和 思考过程

                        // 备份元数据 (关键：保留工具调用的 ID 对接)
                        Map<String, Object> meta = new HashMap<>(msg.getMetadata());
                        if (msg instanceof ToolResponseMessage trm && !trm.getResponses().isEmpty()) {
                            meta.put("toolCallId", trm.getResponses().get(0).id());
                            meta.put("toolName", trm.getResponses().get(0).name());
                        }
                        m.append("metadata", new Document(meta));

                        msgDocs.add(m);
                    }
                }
            }
            cpDoc.append("chatHistory", msgDocs);
            list.add(cpDoc);
        }
        return list;
    }

    private Object parseToBson(String text) {
        if (text == null || text.isBlank()) return text;
        String t = text.trim();
        if ((t.startsWith("{") && t.endsWith("}")) || (t.startsWith("[") && t.endsWith("]"))) {
            try {
                Object parsed = jsonMapper.readValue(t, Object.class);
                return convertToBsonCompatible(parsed);
            } catch (Exception e) {
                return text;
            }
        }
        return text;
    }

    private String bsonToJson(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Document doc) {
            return doc.toJson();
        } else if (obj instanceof List<?> list) {
            // 💡 关键：正确处理 BSON 数组，将其还原为标准的 [{},{}] 字符串
            return "[" + list.stream()
                    .map(item -> (item instanceof Document d) ? d.toJson() : "\"" + item + "\"")
                    .collect(Collectors.joining(",")) + "]";
        }
        return String.valueOf(obj);
    }

    private Object convertToBsonCompatible(Object obj) {
        if (obj instanceof Map<?, ?> map) {
            Document doc = new Document();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Object val = entry.getValue();
                // 递归解析：处理工具返回的字符串嵌套
                if (val instanceof String s && (s.trim().startsWith("{") || s.trim().startsWith("["))) {
                    val = parseToBson(s);
                } else {
                    val = convertToBsonCompatible(val);
                }
                doc.append(String.valueOf(entry.getKey()), val);
            }
            return doc;
        } else if (obj instanceof List<?> list) {
            return list.stream().map(this::convertToBsonCompatible).collect(Collectors.toList());
        }
        return obj;
    }

    private List<Document> serializeLatestSnapshot(Checkpoint cp, RunnableConfig config) {
        Map<String, Object> runMeta = config.metadata().orElse(Collections.emptyMap());
        Document cpDoc = new Document()
                .append("id", cp.getId())
                .append("nodeId", cp.getNodeId())
                .append("nextNodeId", cp.getNextNodeId());

        List<Document> msgDocs = new ArrayList<>();
        Object messagesObj = cp.getState().get("messages");

        if (messagesObj instanceof List<?> msgList) {
            for (Object obj : msgList) {
                if (obj instanceof Message msg) {
                    if (msg.getMessageType() == MessageType.SYSTEM) {
                        continue;
                    }
                    Object thinkStr = msg.getMetadata().get("reasoningContent");
                    boolean isToolCallPending = msg.getMessageType() == MessageType.ASSISTANT
                            && (msg.getText() == null || msg.getText().isBlank())
                            && (thinkStr == null || thinkStr.toString().isBlank())
                            && "TOOL_CALLS".equals(msg.getMetadata().get("finishReason"));

                    if (isToolCallPending) {
                        continue;
                    }
                    Document m = new Document().append("role", msg.getMessageType().name());

                    String rawText = msg.getText();
                    if (msg instanceof ToolResponseMessage trm && !trm.getResponses().isEmpty()) {
                        rawText = trm.getResponses().get(0).responseData();
                    }
                    m.append("content", parseToBson(rawText));
                    if (thinkStr != null && !thinkStr.toString().trim().isBlank()) {
                        m.append("thinking", thinkStr);
                    }
                    m.append("clientId", msg.getMetadata().getOrDefault("clientId", runMeta.get("clientId")));
                    m.append("userId", msg.getMetadata().getOrDefault("userId", runMeta.get("userId")));
                    m.append("aiId", msg.getMetadata().getOrDefault("aiId", runMeta.get("aiId")));
                    Object oldTimestamp = msg.getMetadata().get("timestamp");

                    Date finalDate;
                    if (oldTimestamp instanceof Date date) {
                        finalDate = date;
                    } else if (oldTimestamp instanceof Long ts) {
                        finalDate = new Date(ts);
                    } else if (oldTimestamp instanceof List<?> list && list.size() > 1) {
                        // 专门处理那种 ['java.util.Date', 123456] 的奇葩情况
                        finalDate = new Date(Long.parseLong(list.get(1).toString()));
                    } else {
                        // 如果真的是新消息，才生成当前时间
                        finalDate = new Date();
                    }
                    m.append("timestamp", finalDate);

                    Map<String, Object> meta = new HashMap<>(msg.getMetadata());
                    meta.remove("timestamp");
                    meta.remove("clientId");
                    meta.remove("userId");
                    meta.remove("aiId");
                    meta.remove("reasoningContent");
                    if (msg instanceof ToolResponseMessage trm && !trm.getResponses().isEmpty()) {
                        meta.put("toolCallId", trm.getResponses().get(0).id());
                        meta.put("toolName", trm.getResponses().get(0).name());
                    }
                    m.append("metadata", new Document(meta));
                    msgDocs.add(m);
                }
            }
        }
        cpDoc.append("chatHistory", msgDocs);
        return Collections.singletonList(cpDoc);
    }

    // 替换原来的 deserializeCheckpoints 逻辑
    private LinkedList<Checkpoint> convertFromBsonList(List<Document> stateDocs) {
        LinkedList<Checkpoint> history = new LinkedList<>();
        if (stateDocs == null) return history;

        for (Document doc : stateDocs) {
            List<Document> chatHistory = doc.getList("chatHistory", Document.class);
            List<Message> springMessages = new LinkedList<>();

            for (Document m : chatHistory) {
                String role = m.getString("role");
                Object contentObj = m.get("content");

                // 💡 修复点：调用 bsonToJson 确保还原出的 content 是标准的 JSON 字符串
                String contentStr = bsonToJson(contentObj);

                Map<String, Object> meta = (Map<String, Object>) m.get("metadata");
                if (meta == null) meta = new HashMap<>();
                Object dbThinking = m.get("thinking");
                if (dbThinking != null) {
                    meta.put("reasoningContent", dbThinking);
                }
                meta.put("aiId", m.get("aiId"));
                meta.put("clientId", m.get("clientId"));
                meta.put("userId", m.get("userId"));
                Object dbTimestamp = m.get("timestamp");
                if (dbTimestamp != null) {
                    meta.put("timestamp", dbTimestamp);
                }

                Message springMsg = switch (role) {
                    case "USER" -> new UserMessage(contentStr);
                    case "SYSTEM" -> new SystemMessage(contentStr);
                    case "ASSISTANT" -> {
                        AssistantMessage am = new AssistantMessage(contentStr);
                        if (meta != null) am.getMetadata().putAll(meta);
                        yield am;
                    }
                    case "TOOL" -> {
                        String callId = (String) meta.getOrDefault("toolCallId", "unknown");
                        String name = (String) meta.getOrDefault("toolName", "unknown");
                        ToolResponseMessage.ToolResponse tr = new ToolResponseMessage.ToolResponse(callId, name, contentStr);
                        ToolResponseMessage tm = ToolResponseMessage.builder()
                                .responses(List.of(tr))
                                .build();
                        if (meta != null) tm.getMetadata().putAll(meta);
                        yield tm;
                    }
                    default -> new UserMessage(contentStr);
                };
                if (springMsg instanceof SystemMessage) {
                    springMessages.add(0, springMsg); // 始终放在第一位
                } else {
                    springMessages.add(springMsg);  // 其他消息按顺序放在后面
                }
                springMsg.getMetadata().putAll(meta);
            }

            Map<String, Object> state = new HashMap<>();
            state.put("messages", springMessages);

            history.add(Checkpoint.builder()
                    .id(doc.getString("id"))
                    .nodeId(doc.getString("nodeId"))
                    .nextNodeId(doc.getString("nextNodeId"))
                    .state(state)
                    .build());
        }
        return history;
    }

    private String serializeCheckpoints(List<Checkpoint> checkpoints) throws IOException {
        if (checkpoints == null || checkpoints.isEmpty()) {
            return "[]";
        }
        // 直接将 List 转换为 JSON 字符串存入数据库
        return jsonMapper.writeValueAsString(checkpoints);
    }

    private LinkedList<Checkpoint> deserializeCheckpoints(String content) throws IOException, ClassNotFoundException {
        if (content == null || content.isEmpty() || "[]".equals(content)) {
            return new LinkedList<>();
        }
        // 使用 TypeReference 确保正确反序列化为 LinkedList<Checkpoint>
        return jsonMapper.readValue(content, new TypeReference<LinkedList<Checkpoint>>() {
        });
    }

    /**
     * Gets or creates a thread_id for the given thread_name.
     * If an active thread exists, returns its thread_id.
     * If no active thread exists or the thread is released, creates a new thread_id.
     * <p>
     * This method uses atomic operations to prevent race conditions in concurrent scenarios.
     * Uses findOneAndUpdate with conditional logic to ensure thread-safe creation.
     *
     * @param threadName    the thread name
     * @param clientSession the MongoDB client session for transaction
     * @return the thread_id (UUID string)
     */
    private String getOrCreateThreadId(String threadName, ClientSession clientSession) {
        MongoCollection<Document> threadMetaCollection = database.getCollection(THREAD_META_COLLECTION);
        String metaId = THREAD_META_PREFIX + threadName;

        // Step 1: Try to atomically get an active thread
        // Filter: _id matches AND is_released != true
        Document activeThreadFilter = new Document("_id", metaId)
                .append(FIELD_IS_RELEASED, new Document("$ne", true));

        FindOneAndUpdateOptions findOptions = new FindOneAndUpdateOptions()
                .returnDocument(ReturnDocument.AFTER);

        // Atomically get an active thread (using a no-op update to ensure atomic read)
        Document existingDoc = threadMetaCollection.findOneAndUpdate(
                clientSession,
                activeThreadFilter,
                Updates.currentDate("_lastAccessed"), // No-op update for atomic read
                findOptions
        );

        if (existingDoc != null) {
            String threadId = existingDoc.getString(FIELD_THREAD_ID);
            if (threadId != null) {
                // Active thread exists, return its thread_id
                return threadId;
            }
        }

        // Step 2: No active thread exists, create a new one atomically
        // Strategy: Use findOneAndUpdate with upsert, but handle two cases:
        // a) Document doesn't exist - upsert will create it
        // b) Document exists but is_released == true - update it conditionally

        String newThreadId = UUID.randomUUID().toString();
        FindOneAndUpdateOptions upsertOptions = new FindOneAndUpdateOptions()
                .upsert(true)
                .returnDocument(ReturnDocument.AFTER);

        // First, try to create if document doesn't exist (using upsert)
        // This will create the document if it doesn't exist
        Document createResult = threadMetaCollection.findOneAndUpdate(
                clientSession,
                Filters.eq("_id", metaId), // This matches if exists, or creates if not (with upsert)
                Updates.combine(
                        // Only set these when inserting (document doesn't exist)
                        Updates.setOnInsert(FIELD_THREAD_ID, newThreadId),
                        Updates.setOnInsert(FIELD_IS_RELEASED, false)
                ),
                upsertOptions
        );

        if (createResult != null) {
            Boolean isReleased = createResult.getBoolean(FIELD_IS_RELEASED, false);
            String existingThreadId = createResult.getString(FIELD_THREAD_ID);

            // If document was just created or was already active, return the thread_id
            if (existingThreadId != null && !Boolean.TRUE.equals(isReleased)) {
                return existingThreadId;
            }

            // If document exists but is released, update it atomically
            // Use conditional update to ensure we only update if still released
            if (Boolean.TRUE.equals(isReleased)) {
                Document updateResult = threadMetaCollection.findOneAndUpdate(
                        clientSession,
                        Filters.and(
                                Filters.eq("_id", metaId),
                                Filters.eq(FIELD_IS_RELEASED, true) // Only update if still released
                        ),
                        Updates.combine(
                                Updates.set(FIELD_THREAD_ID, newThreadId),
                                Updates.set(FIELD_IS_RELEASED, false)
                        ),
                        new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
                );

                if (updateResult != null) {
                    return updateResult.getString(FIELD_THREAD_ID);
                }

                // If update failed (another thread already updated it), query again
                Document finalDoc = threadMetaCollection.find(clientSession, new BasicDBObject("_id", metaId)).first();
                if (finalDoc != null) {
                    String finalThreadId = finalDoc.getString(FIELD_THREAD_ID);
                    Boolean finalIsReleased = finalDoc.getBoolean(FIELD_IS_RELEASED, false);
                    if (finalThreadId != null && !Boolean.TRUE.equals(finalIsReleased)) {
                        return finalThreadId;
                    }
                }
            }
        }

        // Final fallback: query again to get the current state
        Document finalDoc = threadMetaCollection.find(clientSession, new BasicDBObject("_id", metaId)).first();
        if (finalDoc != null) {
            String finalThreadId = finalDoc.getString(FIELD_THREAD_ID);
            if (finalThreadId != null) {
                return finalThreadId;
            }
        }

        return newThreadId;
    }

    /**
     * Gets the active thread_id for the given thread_name.
     * Returns null if no active thread exists.
     *
     * @param threadName    the thread name
     * @param clientSession the MongoDB client session for transaction
     * @return the active thread_id, or null if not found
     */
    private String getActiveThreadId(String threadName, ClientSession clientSession) {
        MongoCollection<Document> threadMetaCollection = database.getCollection(THREAD_META_COLLECTION);
        String metaId = THREAD_META_PREFIX + threadName;

        Document metaDoc = threadMetaCollection.find(clientSession, new BasicDBObject("_id", metaId)).first();

        if (metaDoc != null) {
            String threadId = metaDoc.getString(FIELD_THREAD_ID);
            Boolean isReleased = metaDoc.getBoolean(FIELD_IS_RELEASED, false);

            if (threadId != null && !Boolean.TRUE.equals(isReleased)) {
                return threadId;
            }
        }

        return null; // No active thread exists
    }

    @Override
    public Collection<Checkpoint> list(RunnableConfig config) {
        return get(config)
                .map(Collections::singletonList)
                .orElse(Collections.emptyList());
    }

    @Override
    public Optional<Checkpoint> get(RunnableConfig config) {
        String threadName = config.threadId().orElseThrow(() -> new IllegalArgumentException("threadId null"));
        ClientSession clientSession = this.client.startSession(ClientSessionOptions.builder().defaultTransactionOptions(txnOptions).build());
        try {
            clientSession.startTransaction();
            String threadId = getActiveThreadId(threadName, clientSession);
            if (threadId == null) return Optional.empty();

            Document doc = database.getCollection(CHECKPOINT_COLLECTION)
                    .find(clientSession, Filters.eq("_id", CHECKPOINT_PREFIX + threadId)).first();

            if (doc == null) return Optional.empty();

            List<Document> bsonList = doc.getList(DOCUMENT_CONTENT_KEY, Document.class);
            LinkedList<Checkpoint> history = convertFromBsonList(bsonList);
            clientSession.commitTransaction();

            return Optional.ofNullable(history.peekLast());
        } catch (Exception e) {
            if (clientSession.hasActiveTransaction()) clientSession.abortTransaction();
            logger.error("Get Checkpoint Failed", e);
            return Optional.empty();
        } finally {
            clientSession.close();
        }
    }

    @Override
    public RunnableConfig put(RunnableConfig config, Checkpoint checkpoint) throws Exception {
        String threadName = config.threadId().orElseThrow(() -> new IllegalArgumentException("threadId cannot be null"));
        ClientSession clientSession = this.client.startSession(ClientSessionOptions.builder().defaultTransactionOptions(txnOptions).build());
        clientSession.startTransaction();
        try {
            String threadId = getOrCreateThreadId(threadName, clientSession);
            String checkpointDocId = CHECKPOINT_PREFIX + threadId;
            Map<String, Object> runMeta = config.metadata().orElse(Collections.emptyMap());

            Document finalDoc = new Document()
                    .append("_id", checkpointDocId)
                    .append("conversationId", runMeta.get("conversationId"))
                    .append(DOCUMENT_CONTENT_KEY, serializeLatestSnapshot(checkpoint, config));

            database.getCollection(CHECKPOINT_COLLECTION).replaceOne(
                    clientSession, Filters.eq("_id", checkpointDocId), finalDoc, new ReplaceOptions().upsert(true));

            clientSession.commitTransaction();
            return RunnableConfig.builder(config).checkPointId(checkpoint.getId()).build();
        } catch (Exception e) {
            clientSession.abortTransaction();
            throw e;
        } finally {
            clientSession.close();
        }
    }

    @Override
    public BaseCheckpointSaver.Tag release(RunnableConfig config) throws Exception {
        Optional<String> threadNameOpt = config.threadId();
        if (!threadNameOpt.isPresent()) {
            throw new IllegalArgumentException("threadId is not allow null");
        }

        String threadName = threadNameOpt.get();
        ClientSession clientSession = this.client
                .startSession(ClientSessionOptions.builder().defaultTransactionOptions(txnOptions).build());
        clientSession.startTransaction();
        try {
            MongoCollection<Document> threadMetaCollection = database.getCollection(THREAD_META_COLLECTION);
            String metaId = THREAD_META_PREFIX + threadName;

            // 1. 获取 ThreadId 并标记释放
            Document metaDoc = threadMetaCollection.find(clientSession, Filters.eq("_id", metaId)).first();
            if (metaDoc == null) {
                clientSession.abortTransaction();
                throw new IllegalStateException("Thread not found: " + threadName);
            }

            String threadId = metaDoc.getString(FIELD_THREAD_ID);

            // 原子标记 thread 为已释放状态
            Document releaseFilter = new Document("_id", metaId)
                    .append(FIELD_IS_RELEASED, false);

            Document updatedDoc = threadMetaCollection.findOneAndUpdate(
                    clientSession,
                    releaseFilter,
                    Updates.set(FIELD_IS_RELEASED, true),
                    new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
            );

            if (updatedDoc == null) {
                clientSession.abortTransaction();
                throw new IllegalStateException("Thread is not active or already released: " + threadName);
            }

            // 2. 💡 关键修改点：从 Checkpoint 集合获取并解析 BSON 列表
            MongoCollection<Document> checkpointCollection = database.getCollection(CHECKPOINT_COLLECTION);
            String checkpointDocId = CHECKPOINT_PREFIX + threadId;
            Document checkpointDoc = checkpointCollection.find(clientSession, Filters.eq("_id", checkpointDocId))
                    .first();

            Collection<Checkpoint> checkpoints = Collections.emptyList();
            if (checkpointDoc != null) {
                // 从 Document 中提取 List<Document> 格式的内容
                List<Document> bsonList = checkpointDoc.getList(DOCUMENT_CONTENT_KEY, Document.class);
                if (bsonList != null) {
                    // 使用你已经写好的 convertFromBsonList 进行还原
                    checkpoints = convertFromBsonList(bsonList);
                }
            }

            clientSession.commitTransaction();
            return new BaseCheckpointSaver.Tag(threadName, checkpoints);

        } catch (Exception e) {
            if (clientSession.hasActiveTransaction()) {
                clientSession.abortTransaction();
            }
            logger.error("Release Checkpoint Failed", e);
            throw new RuntimeException(e);
        } finally {
            clientSession.close();
        }
    }

    /**
     * Builder class for MongoSaver.
     */
    public static class Builder {
        private MongoClient client;
        private StateSerializer stateSerializer;

        public AiChatMongoSaver.Builder client(MongoClient client) {
            this.client = client;
            return this;
        }

        public AiChatMongoSaver.Builder stateSerializer(StateSerializer stateSerializer) {
            this.stateSerializer = stateSerializer;
            return this;
        }

        /**
         * Builds a new MongoSaver instance.
         *
         * @return a new MongoSaver instance
         * @throws IllegalArgumentException if client or stateSerializer is null
         */
        public AiChatMongoSaver build() {
            if (client == null) {
                throw new IllegalArgumentException("client cannot be null");
            }
            if (stateSerializer == null) {
                this.stateSerializer = StateGraph.DEFAULT_JACKSON_SERIALIZER;
            }
            return new AiChatMongoSaver(client, stateSerializer);
        }
    }
}
