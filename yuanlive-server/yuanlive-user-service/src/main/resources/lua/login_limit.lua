-- 失败次数key
local fail_count_key = KEYS[1]
-- 锁定key
local lock_key = KEYS[2]

local threshold = tonumber(ARGV[1])
local count_expire = tonumber(ARGV[2])
local lock_expire = tonumber(ARGV[3])

local lock_time = redis.call('ttl', lock_key)
if lock_time > 0 then
    return lock_time
end

local current_count = redis.call('incr', fail_count_key)
if current_count == 1 then
    redis.call('expire', fail_count_key, count_expire)
end

if current_count >= threshold then
    redis.call('set', lock_key, 'locked', 'EX', lock_expire)
    redis.call('del', fail_count_key)
    return lock_expire
end

return 0