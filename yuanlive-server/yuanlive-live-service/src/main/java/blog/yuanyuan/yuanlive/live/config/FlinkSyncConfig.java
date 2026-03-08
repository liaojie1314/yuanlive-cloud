package blog.yuanyuan.yuanlive.live.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class FlinkSyncConfig implements ApplicationRunner {
    private final String GATEWAY_URL = "http://localhost:8063/v1/";
    private final String JOBS_OVERVIEW_URL = "http://localhost:8061/jobs/overview";
    private final String TARGET_JOB_NAME = "yuanlive-sync-job";
    private final RestTemplate restTemplate = new RestTemplate();

    private void startSyncJob() {
        // 1. 检查任务是否已经在运行 (避免重复提交)
        if (isJobRunning()) {
            log.info("检测到 Flink 任务 [{}] 已经在运行中，跳过本次提交。", TARGET_JOB_NAME);
            return;
        }

        try {
            log.info("准备向 Flink Gateway 提交同步任务...");

            // 2. 创建 Session
            String sessionHandle = createSession();
            if (sessionHandle == null) return;

            // 3. 准备 SQL 语句
            String[] sqls = {
                    "SET 'pipeline.name' = '" + TARGET_JOB_NAME + "'", // 给任务命名
                    "SET 'execution.checkpointing.interval' = '1min'", // 开启 Checkpoint 保证容器重启可恢复

                    "CREATE TABLE IF NOT EXISTS mysql_user (\n" +
                            "    uid BIGINT,\n" +
                            "    username STRING,\n" +
                            "    PRIMARY KEY (uid) NOT ENFORCED\n" +
                            ") WITH (\n" +
                            "    'connector' = 'mysql-cdc',\n" +
                            "    'hostname' = 'mysql',\n" +
                            "    'port' = '3306',\n" +
                            "    'username' = 'root',\n" +
                            "    'password' = 'yuanlive',\n" +
                            "    'database-name' = 'yuanlive_user',\n" +
                            "    'table-name' = 'sys_user',\n" +
                            "    'server-time-zone' = 'Asia/Shanghai'\n" +
                            ")",

                    "CREATE TABLE IF NOT EXISTS mysql_room (\n" +
                            "    id BIGINT,\n" +
                            "    anchor_id BIGINT,\n" +
                            "    title STRING,\n" +
                            "    category_id INT,\n" +
                            "    cover_img STRING,\n" +
                            "    create_time TIMESTAMP(3),\n" +
                            "    PRIMARY KEY (id) NOT ENFORCED\n" +
                            ") WITH (\n" +
                            "    'connector' = 'mysql-cdc',\n" +
                            "    'hostname' = 'mysql',\n" +
                            "    'port' = '3306',\n" +
                            "    'username' = 'root',\n" +
                            "    'password' = 'yuanlive',\n" +
                            "    'database-name' = 'yuanlive_live',\n" +
                            "    'table-name' = 'live_room',\n" +
                            "    'server-time-zone' = 'Asia/Shanghai'\n" +
                            ")",

                    "CREATE TABLE IF NOT EXISTS mysql_video (\n" +
                            "    id BIGINT,\n" +
                            "    user_id BIGINT,\n" +
                            "    room_id BIGINT,\n" +
                            "    title STRING,\n" +
                            "    video_url STRING,\n" +
                            "    cover_url STRING,\n" +
                            "    create_time TIMESTAMP(3),\n" +
                            "    description STRING,\n" +
                            "    PRIMARY KEY (id) NOT ENFORCED\n" +
                            ") WITH (\n" +
                            "    'connector' = 'mysql-cdc',\n" +
                            "    'hostname' = 'mysql',\n" +
                            "    'port' = '3306',\n" +
                            "    'username' = 'root',\n" +
                            "    'password' = 'yuanlive',\n" +
                            "    'database-name' = 'yuanlive_live',\n" +
                            "    'table-name' = 'video_resource',\n" +
                            "    'server-time-zone' = 'Asia/Shanghai'\n" +
                            ")",

                    "CREATE TABLE IF NOT EXISTS mysql_category (\n" +
                            "    id INT,\n" +
                            "    name STRING,\n" +
                            "    PRIMARY KEY (id) NOT ENFORCED\n" +
                            ") WITH (\n" +
                            "    'connector' = 'mysql-cdc',\n" +
                            "    'hostname' = 'mysql',\n" +
                            "    'port' = '3306',\n" +
                            "    'username' = 'root',\n" +
                            "    'password' = 'yuanlive',\n" +
                            "    'database-name' = 'yuanlive_live',\n" +
                            "    'table-name' = 'live_category',\n" +
                            "    'server-time-zone' = 'Asia/Shanghai'\n" +
                            ")",

                    "CREATE TABLE IF NOT EXISTS es_search_sink (\n" +
                            "    id BIGINT,\n" +
                            "    uid BIGINT,\n" +
                            "    biz_type INT,\n" +
                            "    title STRING,\n" +
                            "    anchor_name STRING,\n" +
                            "    room_title STRING,\n" +
                            "    category_id INT,\n" +
                            "    category_name STRING,\n" +
                            "    cover_url STRING,\n" +
                            "    video_url STRING,\n" +
                            "    hot_score DOUBLE,\n" +
                            "    create_time TIMESTAMP(3),\n" +
                            "    description STRING,\n" +
                            "    suggestion STRING,\n" +
                            "    PRIMARY KEY (id) NOT ENFORCED\n" +
                            ") WITH (\n" +
                            "    'connector' = 'elasticsearch-7',\n" +
                            "    'hosts' = 'http://es:9200',\n" +
                            "    'index' = 'yuanlive_search',\n" +
                            "    'format' = 'json',\n" +
                            "    'sink.flush-on-checkpoint' = 'false', \n" +
                            "    'failure-handler' = 'ignore'\n" +     // 去掉 sink.
                            ")",

                    "INSERT INTO es_search_sink " +
                            "SELECT lr.id, lr.anchor_id, 1, lr.title, su.username, lr.title, lr.category_id, lc.name, lr.cover_img, CAST(NULL AS STRING), 0.0, " +
                            "COALESCE(lr.create_time, TO_TIMESTAMP('1970-01-01 00:00:00')), CAST(NULL AS STRING), lr.title " +
                            "FROM mysql_room lr LEFT JOIN mysql_user su ON lr.anchor_id = su.uid LEFT JOIN mysql_category lc ON lr.category_id = lc.id " +
                            "UNION ALL " +
                            "SELECT vr.id, vr.user_id, 2, vr.title, su.username, lr.title, lr.category_id, lc.name, vr.cover_url, vr.video_url, 0.0, " +
                            "COALESCE(vr.create_time, TO_TIMESTAMP('1970-01-01 00:00:00')), vr.description, vr.title " +
                            "FROM mysql_video vr LEFT JOIN mysql_user su ON vr.user_id = su.uid LEFT JOIN mysql_room lr ON vr.room_id = lr.id LEFT JOIN mysql_category lc ON lr.category_id = lc.id " +
                            "WHERE vr.video_url IS NOT NULL"
            };

            // 4. 提交 SQL
            String statementUrl = GATEWAY_URL + "sessions/" + sessionHandle + "/statements";
            for (String sql : sqls) {
                Map<String, String> body = new HashMap<>();
                body.put("statement", sql);
                Map resp = restTemplate.postForObject(statementUrl, body, Map.class);

                // 校验返回，如果报错则停止
                if (resp == null || (resp.get("operationHandle") == null && resp.get("operation_handle") == null)) {
                    log.error("SQL 提交异常，响应内容: {}", resp);
                    break;
                }
                log.info("SQL 已执行: {}", sql.substring(0, Math.min(20, sql.length())));
            }

            log.info("Flink同步任务流水线处理完毕！");
        } catch (Exception e) {
            log.error("Flink任务提交流程发生错误: ", e);
        }
    }

    // 检查 Flink 集群中是否已经有同名任务在运行
    private boolean isJobRunning() {
        try {
            Map resp = restTemplate.getForObject(JOBS_OVERVIEW_URL, Map.class);
            if (resp != null && resp.containsKey("jobs")) {
                List<Map> jobs = (List<Map>) resp.get("jobs");
                return jobs.stream().anyMatch(job ->
                        TARGET_JOB_NAME.equals(job.get("name")) && "RUNNING".equals(job.get("state")));
            }
        } catch (Exception e) {
            log.warn("检查 Flink 任务状态失败，可能是集群尚未完全启动。");
        }
        return false;
    }

    private String createSession() {
        String sessionUrl = GATEWAY_URL + "sessions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>("{}", headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(sessionUrl, entity, Map.class);
            Map body = response.getBody();
            if (body != null) {
                return body.containsKey("sessionHandle") ?
                        (String) body.get("sessionHandle") : (String) body.get("session_handle");
            }
        } catch (Exception e) {
            log.error("创建 Flink Session 失败: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public void run(ApplicationArguments args) {
        // 延迟执行，给 Flink 容器一点启动时间
        new Thread(() -> {
            try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
            startSyncJob();
        }).start();
    }
}
