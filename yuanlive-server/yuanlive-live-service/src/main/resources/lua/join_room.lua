-- KEYS[1]: currentKey (Set)
-- KEYS[2]: sessionKey (Hash)
-- KEYS[3]: totalKey (HyperLogLog)
-- ARGV[1]: userId

-- 1. 加入当前在线 Set
redis.call('sadd', KEYS[1], ARGV[1])

-- 2. 加入累计观看 HLL
redis.call('pfadd', KEYS[3], ARGV[1])

-- 3. 获取当前在线人数
local currentCount = redis.call('scard', KEYS[1])

-- 4. 获取并更新峰值
local peak = redis.call('hget', KEYS[2], 'peak')
if not peak or tonumber(currentCount) > tonumber(peak) then
    redis.call('hset', KEYS[2], 'peak', currentCount)
end

-- 5. 返回最新的在线人数
return currentCount