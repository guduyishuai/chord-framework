package com.chord.framework.boot.autoconfigure.mutex.lock.properties;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Created on 2020/6/28
 *
 * @author: wulinfeng
 */
public class RedisDataSource {

    @NestedConfigurationProperty
    private RedisProperties connection;

    private String lockKey = "chord_lock";

    private long lockExpireTime = 6;

    public RedisProperties getConnection() {
        return connection;
    }

    public void setConnection(RedisProperties connection) {
        this.connection = connection;
    }

    public String getLockKey() {
        return lockKey;
    }

    public void setLockKey(String lockKey) {
        this.lockKey = lockKey;
    }

    public long getLockExpireTime() {
        return lockExpireTime;
    }

    public void setLockExpireTime(long lockExpireTime) {
        this.lockExpireTime = lockExpireTime;
    }
}
