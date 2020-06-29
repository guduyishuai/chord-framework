package com.chord.framework.mutex.lock.redis;

import lombok.Data;

/**
 * Created on 2020/6/24
 *
 * @author: wulinfeng
 */
@Data
public class RedisConfiguration {

    private String lockKey;
    private LockValueGenerator lockValueGenerator;
    private long lockExpireTime;

}
