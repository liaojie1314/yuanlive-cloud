-- KEYS[1]: currentKey (Set)
-- KEYS[2]: rankingKey (ZSet)
-- ARGV[1]: userId
-- ARGV[2]: roomId
-- ARGV[3]: viewWeight (减去的人气权重)

-- 1. 尝试从 Set 中移除用户
local removed = redis.call('srem', KEYS[1], ARGV[1])

-- 2. 只有移除成功(之前在里面)，才减去人气分
if removed == 1 then
    redis.call('zincrby', KEYS[2], -tonumber(ARGV[3]), ARGV[2])
    -- 获取剩余人数
    return redis.call('scard', KEYS[1])
end

return -1 -- 表示用户不在该房间，无需处理