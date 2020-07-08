package com.chord.framework.boot.autoconfigure.mutex.lock;

import com.chord.framework.boot.autoconfigure.mutex.lock.properties.MutexLockProperties;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;

/**
 * Created on 2020/7/6
 *
 * @author: wulinfeng
 */
class RedisPropertiesResolver {

    static RedisProperties resolve(MutexLockProperties properties) {
        if(properties.getRedis() == null) {
            throw new IllegalArgumentException("not found the config for redis");
        }
        return properties.getRedis().getConnection();
    }

}
