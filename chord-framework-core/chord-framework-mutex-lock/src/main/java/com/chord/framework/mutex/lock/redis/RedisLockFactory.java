package com.chord.framework.mutex.lock.redis;

import com.chord.framework.mutex.lock.Lock;
import com.chord.framework.mutex.lock.LockFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Created on 2020/6/24
 *
 * @author: wulinfeng
 */
public class RedisLockFactory implements LockFactory {

    private final RedisConfiguration redisConfiguration;

    private final RedisTemplate redisTemplate;

    public RedisLockFactory(RedisTemplate redisTemplate, RedisConfiguration redisConfiguration) {
        this.redisTemplate = redisTemplate;
        this.redisConfiguration = redisConfiguration;
    }

    @Override
    public Lock create(String name) {
        RedisLock redisLock = new RedisLock(redisTemplate, name);
        redisLock.setLockKey(redisConfiguration.getLockKey());
        redisLock.setLockValueGenerator(redisConfiguration.getLockValueGenerator());
        redisLock.setLockExpireTime(redisConfiguration.getLockExpireTime());
        return redisLock;
    }

}
