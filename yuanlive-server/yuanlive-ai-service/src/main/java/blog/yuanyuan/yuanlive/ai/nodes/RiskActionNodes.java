package blog.yuanyuan.yuanlive.ai.nodes;

import blog.yuanyuan.yuanlive.ai.strategy.RiskStrategies;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class RiskActionNodes {
    @Resource
    private ChatModel nvidiaModel;

    public static final String systemPrompt = """
    你是一名经验丰富的直播间风控专家。你的任务是分析对话内容并给出【违规程度分值】。
    
    ### 核心判定标准（高分项）：
    1. 政治敏感：涉及政治人物、政策攻击、非法集会等（直接 90-100分）。
    2. 色情低俗：露骨的性暗示、招嫖信息、淫秽色情描述（直接 85-100分）。
    3. 恶意辱骂：针对他人的严重人格侮辱、诅咒、仇恨言论（70-90分）。注意：普通的调侃、兄弟间的口嗨、无恶意的脏话不在此列。
    4. 违规网址：引导至非法博彩、诈骗、杀猪盘或站外交易的链接/联系方式（80-100分）。

    ### 噪音过滤标准（低分项）：
    - 幽默调侃：观众间的互动玩笑、梗、不带恶意的吐槽，即使包含轻微粗俗词汇，也应判定在 20分以下。
    - 情绪表达：如“我操这球进了”，属于语气助词，不属于辱骂，应判定为 0分。

    ### 输出要求：
    - 必须忽略轻微的互动噪音。
    - 必须对恶意行为严厉打击。
    - 只返回一个 0 到 100 之间的整数，不要任何解释。
    """;

    public NodeAction analyze() {
        return state -> {
            String history = state.value(RiskStrategies.CHAT_HISTORY)
                    .map(Object::toString).orElse("");
            String prompt = systemPrompt + history;
            String result = nvidiaModel.call(prompt);
            int score = Integer.parseInt(result.replaceAll("[^0-9]", ""));
            return Map.of("risk_score", score);
        };
    }

    // 2. 警告主播
    public NodeAction warn() {
        return state -> {
            System.out.println(">>> [低风险] 已向主播发送私信警告。");
            return Map.of("last_action", "WARNED");
        };
    }

    // 3. 直播间公告
    public NodeAction broadcast() {
        return state -> {
            System.out.println(">>> [中风险] 已在直播间公屏发布合规公告。");
            return Map.of("last_action", "BROADCASTED");
        };
    }

    // 4. 封禁直播间
    public NodeAction ban() {
        return state -> {
            System.out.println(">>> [高风险] 最终裁决：已执行物理断流封禁。");
            return Map.of("last_action", "BAN");
        };
    }
}
