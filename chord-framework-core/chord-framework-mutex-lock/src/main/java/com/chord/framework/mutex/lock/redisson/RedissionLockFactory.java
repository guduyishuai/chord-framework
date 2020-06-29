package com.chord.framework.mutex.lock.redisson;

import com.chord.framework.mutex.lock.Lock;
import com.chord.framework.mutex.lock.LockFactory;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;

/**
 * Created on 2020/6/23
 *
 * @author: wulinfeng
 */
public class RedissionLockFactory implements LockFactory {

    private RedissionConfiguration redissionConfiguration;

    private RedissonClient redisson;

    public RedissionLockFactory(RedissionConfiguration redissionConfiguration) {
        this.redissionConfiguration = redissionConfiguration;
    }

    public RedissionLockFactory(RedissonClient redisson) {
        this.redisson = redisson;
    }

    @Override
    public Lock create(String name) {
        if(redisson == null) {
            redisson = Redisson.create(redissionConfiguration.getConfig());
        }
        return new RedissionLock(redisson.getLock(name), name);
    }

}
