package blog.yuanyuan.yuanlive.ai.config;

import blog.yuanyuan.yuanlive.ai.nodes.RiskActionNodes;
import blog.yuanyuan.yuanlive.ai.strategy.RiskStrategies;
import com.alibaba.cloud.ai.graph.CompileConfig;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.StateGraph;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

import static com.alibaba.cloud.ai.graph.action.AsyncEdgeAction.edge_async;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class RiskWorkflowConfig {
    @SneakyThrows
    @Bean
    public CompiledGraph riskWorkflow(RiskActionNodes nodes) {
        StateGraph graph = new StateGraph(RiskStrategies.create());
        graph.addNode("analyze", node_async(nodes.analyze()));
        graph.addNode("warn", node_async(nodes.warn()));
        graph.addNode("broadcast", node_async(nodes.broadcast()));
        graph.addNode("ban", node_async(nodes.ban()));
        // 审核占位符节点
        graph.addNode("admin_review", node_async(state -> Map.of()));

        graph.addEdge(StateGraph.START, "analyze");
        graph.addConditionalEdges(
                "analyze",
                edge_async(state -> {
                    int score = state.value(RiskStrategies.RISK_SCORE, 0);
                    if (score >= 80) return "high";   // 映射到 admin_review
                    if (score >= 50) return "medium"; // 映射到 broadcast
                    if (score >= 20) return "low";    // 映射到 warn
                    return "safe";
                }),
                Map.of("high", "admin_review",
                        "medium", "broadcast",
                        "low", "warn",
                        "safe", StateGraph.END));
        graph.addConditionalEdges("admin_review",
                edge_async(state -> state.value(RiskStrategies.ADMIN_APPROVED, false) ? "yes" : "no"),
                Map.of("yes", "ban",
                        "no", StateGraph.END));
        graph.addEdge("broadcast", "warn");
        graph.addEdge("warn", StateGraph.END);
        graph.addEdge("ban", StateGraph.END);

        CompileConfig compileConfig = CompileConfig.builder()
                .interruptBefore("admin_review")
                .build();
        return graph.compile(compileConfig);
    }
}
