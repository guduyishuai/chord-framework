package com.chord.framework.mutex.lock.redisson;

import com.chord.framework.mutex.lock.AbstractLock;
import org.redisson.api.RLock;

import java.util.concurrent.TimeUnit;

/**
 * Created on 2020/6/23
 *
 * @author: wulinfeng
 */
public class RedissionLock extends AbstractLock<RLock> {

    public RedissionLock(RLock delegate) {
        super(delegate);
    }

    public RedissionLock(RLock delegate, String name) {
        super(delegate, name);
    }

    @Override
    public void acquire() {
        delegate.lock();
    }

    @Override
    public boolean acquire(long time, TimeUnit unit) {
        delegate.lock(time, unit);
        return delegate.isLocked();
    }

    @Override
    public void release() {
        delegate.unlock();
    }

}
