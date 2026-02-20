package blog.yuanyuan.yuanlive.ai.strategy;

import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;

import java.util.HashMap;

public class RiskStrategies {
    public static final String  RISK_SCORE = "risk_score";
    public static final String  ROOM_ID = "room_id";
    public static final String  ADMIN_APPROVED = "admin_approved";
    public static final String  CHAT_HISTORY = "chat_history";

    public static KeyStrategyFactory create() {
        return () -> {
            HashMap<String, KeyStrategy> map = new HashMap<>();
            map.put(RISK_SCORE, KeyStrategy.REPLACE);
            map.put(ROOM_ID, KeyStrategy.REPLACE);
            map.put(ADMIN_APPROVED, KeyStrategy.REPLACE);
            map.put(CHAT_HISTORY, KeyStrategy.APPEND);
            return map;
        };
    }
}
