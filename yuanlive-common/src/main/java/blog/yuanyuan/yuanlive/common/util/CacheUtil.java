package blog.yuanyuan.yuanlive.common.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Component
@ConditionalOnProperty(name = "spring.data.redis.host")
public class CacheUtil {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public <T,ID> T getCache(String key, Class<T> returnType, ID id, Function<ID, T> dbFetch, long ttl, TimeUnit timeUnit) {
        String jsonStr = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(jsonStr)) {
            return JSONUtil.toBean(jsonStr, returnType);
        }
        // 从数据库中获取
        T result = dbFetch.apply(id);
        if (result != null) {
            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(result), ttl, timeUnit);
        }
        return result;
    }

    public <T,ID> List<T> getCacheList (String key, Class<T> ofType, ID id, Function<ID,List<T>> dbFetch, long ttl, TimeUnit timeunit) {
        String jsonStr = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(jsonStr)) {
            return JSONUtil.toList(jsonStr, ofType);
        }
        List<T> result = dbFetch.apply(id);
        if (result != null) {
            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(result), ttl, timeunit);
        }
        return result;
    }
}
