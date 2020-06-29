--
-- Created by IntelliJ IDEA.
-- User: 86188
-- Date: 2020/6/28
-- Time: 15:52
-- To change this template use File | Settings | File Templates.
--

if redis.call('get', KEYS[1]) == ARGV[1] then
    return redis.call('del', KEYS[1])
else
    return 0
end