-- KEYS[1]: 聊天记录的 List Key
-- ARGV[1]: 水位线阈值 (例如 50)
local list_len = redis.call('LLEN', KEYS[1])
if list_len >= tonumber(ARGV[1]) then
    local all_messages = redis.call('LRANGE', KEYS[1], 0, -1)
    redis.call('DEL', KEYS[1])
    return all_messages
else
    return {}
end