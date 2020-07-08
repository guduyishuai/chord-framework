package com.chord.framework.boot.autoconfigure.mutex.lock.properties;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;

/**
 * Created on 2020/6/28
 *
 * @author: wulinfeng
 */
public class RedisDataSource {

    @NestedConfigurationProperty
    private RedisProperties connection;

    @NotEmpty
    private String lockKey = "chord_lock";

    @Positive
    @Digits(integer = 12, fraction = 0)
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
