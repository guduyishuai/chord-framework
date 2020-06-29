package com.chord.framework.mutex.lock.redis;

import com.chord.framework.mutex.lock.AbstractLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.io.Serializable;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2020/6/24
 *
 * @author: wulinfeng
 */
public class RedisLock extends AbstractLock<RedisTemplate> {

    private final static Logger logger = LoggerFactory.getLogger(RedisLock.class);

    private String lockKey;

    private Serializable lockValue;

    private LockValueGenerator lockValueGenerator;

    private RedisScript<Long> lockReleaseScript;

    private RedisScript<Long> lockAcquireScript;

    private long lockExpireTime;

    public RedisLock(RedisTemplate delegate) {
        super(delegate);
        initScript();
    }

    public RedisLock(RedisTemplate delegate, String name) {
        super(delegate, name);
        initScript();
    }

    @Override
    public void acquire() {
        lock();
    }

    @Override
    public boolean acquire(long time, TimeUnit unit) {
        long currentTimeMillis = System.currentTimeMillis();
        long endTime = currentTimeMillis;
        switch (unit) {
            case SECONDS: endTime += time * 1000; break;
            case MINUTES: endTime += time * 60 * 1000; break;
            case MILLISECONDS: endTime += time; break;
            case DAYS: endTime += time * 24 * 60 * 60 * 1000; break;
            case HOURS: endTime += time * 60 * 60 * 1000; break;
            case NANOSECONDS: throw new IllegalArgumentException("不支持纳秒");
            case MICROSECONDS: throw new IllegalArgumentException("不支持微妙");
            default: break;
        }
        boolean acquired = false;
        while(!acquired) {
            try {
                if(currentTimeMillis > endTime) {
                    logger.warn("acquire lock timeout");
                    return false;
                }
                acquired = lock();
            } catch (Exception e) {
               logger.error("acquire lock failed", e);
                return false;
            }
            currentTimeMillis = System.currentTimeMillis();
        }
        return true;
    }

    @Override
    public void release() {
        delegate.execute(this.lockReleaseScript, Collections.singletonList(lockKey), lockValue);
    }

    private void initScript() {
        this.lockAcquireScript = new DefaultRedisScript<>();
        this.lockReleaseScript = new DefaultRedisScript<>();
        ((DefaultRedisScript<Long>) this.lockAcquireScript).setResultType(Long.class);
        ((DefaultRedisScript) this.lockAcquireScript).setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/lock_acquire.lua")));
        ((DefaultRedisScript<Long>) this.lockReleaseScript).setResultType(Long.class);
        ((DefaultRedisScript) this.lockReleaseScript).setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/lock_release.lua")));
    }

    private boolean lock() {
        this.lockValue = lockValueGenerator.generate();
        return Optional.ofNullable(
                delegate.execute(
                    this.lockAcquireScript,
                    Collections.singletonList(lockKey),
                    lockValue,
                    String.valueOf(lockExpireTime)))
                .map(result -> ((Long) result) == 1)
                .orElse(false);
    }

    public String getLockKey() {
        return lockKey;
    }

    public void setLockKey(String lockKey) {
        this.lockKey = lockKey;
    }

    public LockValueGenerator getLockValueGenerator() {
        return lockValueGenerator;
    }

    public void setLockValueGenerator(LockValueGenerator lockValueGenerator) {
        this.lockValueGenerator = lockValueGenerator;
    }

    public long getLockExpireTime() {
        return lockExpireTime;
    }

    public void setLockExpireTime(long lockExpireTime) {
        this.lockExpireTime = lockExpireTime;
    }
}
