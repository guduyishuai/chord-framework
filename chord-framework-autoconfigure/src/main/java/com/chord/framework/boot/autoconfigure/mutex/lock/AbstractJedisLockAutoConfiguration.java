package com.chord.framework.boot.autoconfigure.mutex.lock;

import com.chord.framework.boot.autoconfigure.common.redis.AbstractJedisAutoConfiguration;
import com.chord.framework.boot.autoconfigure.mutex.lock.properties.MutexLockProperties;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;

/**
 * Created on 2020/6/29
 *
 * @author: wulinfeng
 */
public class AbstractJedisLockAutoConfiguration extends AbstractJedisAutoConfiguration<MutexLockProperties> {

    public AbstractJedisLockAutoConfiguration(MutexLockProperties mutexLockProperties) {
        super(mutexLockProperties);
    }

    @Override
    protected RedisProperties getRedisProperties(MutexLockProperties properties) {
        return RedisPropertiesResolver.resolve(properties);
    }

}
