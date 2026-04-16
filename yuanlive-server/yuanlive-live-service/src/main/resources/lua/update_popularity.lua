-- KEYS[1]: 房间榜 Key (main-rank)
-- KEYS[2]: 分类榜 Key (category-rank)
-- KEYS[3]: sessionKey (获取其中的 categoryId)
-- ARGV[1]: 房间 ID (roomId)
-- ARGV[2]: 增加的分数 (increment)

-- 1. 更新房间人气
redis.call('zincrby', KEYS[1], ARGV[2], ARGV[1])

-- 2. 查找房间对应的分类 ID
local categoryId = redis.call('hget', KEYS[3], 'categoryId')

-- 3. 如果找到了分类，同步更新分类人气
if categoryId then
    redis.call('zincrby', KEYS[2], ARGV[2], categoryId)
end

return 1