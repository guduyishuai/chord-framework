package com.chord.framework.boot.autoconfigure.mutex.lock;

import com.chord.framework.boot.autoconfigure.common.redis.AbstractLettuceAutoConfiguration;
import com.chord.framework.boot.autoconfigure.mutex.lock.properties.MutexLockProperties;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;

/**
 * Created on 2020/6/29
 *
 * @author: wulinfeng
 */
public class AbstractLettuceLockAutoConfiguration extends AbstractLettuceAutoConfiguration<MutexLockProperties> {

    public AbstractLettuceLockAutoConfiguration(MutexLockProperties mutexLockProperties) {
        super(mutexLockProperties);
    }

    @Override
    protected RedisProperties getRedisProperties(MutexLockProperties properties) {
        return RedisPropertiesResolver.resolve(properties);
    }

}
