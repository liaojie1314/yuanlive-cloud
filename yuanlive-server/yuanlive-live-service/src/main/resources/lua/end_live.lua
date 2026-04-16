-- KEYS[1]: 房间榜 Key
-- KEYS[2]: 分类榜 Key
-- KEYS[3]: sessionKey
-- ARGV[1]: 房间 ID (roomId)

-- 1. 获取该房间当前的人气分数
local currentScore = redis.call('zscore', KEYS[1], ARGV[1])

-- 2. 获取该房间所属的分类 ID
local categoryId = redis.call('hget', KEYS[3], 'categoryId')

-- 3. 只有当分数和分类都存在时，才进行扣减
if currentScore and categoryId then
    -- 将分数转为负数，从分类榜中扣除
    redis.call('zincrby', KEYS[2], -tonumber(currentScore), categoryId)

    -- 4. 清理数据：从房间榜移除该房间
    redis.call('zrem', KEYS[1], ARGV[1])

    -- 如果分类扣减后分数小于等于 0，可以选择直接从榜单移除该分类成员
    local catScore = redis.call('zscore', KEYS[2], categoryId)
    if catScore and tonumber(catScore) <= 0 then
        redis.call('zrem', KEYS[2], categoryId)
    end
    return 1 -- 成功处理
end

return 0 -- 未找到相关记录