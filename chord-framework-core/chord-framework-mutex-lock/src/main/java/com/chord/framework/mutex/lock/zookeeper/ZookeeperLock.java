package com.chord.framework.mutex.lock.zookeeper;

import com.chord.framework.mutex.lock.AbstractLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;

import java.util.concurrent.TimeUnit;

/**
 * Created on 2020/6/23
 *
 * @author: wulinfeng
 */
public class ZookeeperLock extends AbstractLock<InterProcessMutex> {

    public ZookeeperLock(InterProcessMutex delegate) {
        super(delegate);
    }

    public ZookeeperLock(InterProcessMutex delegate, String name) {
        super(delegate, name);
    }

    @Override
    public void acquire() throws Exception {
        delegate.acquire();
    }

    @Override
    public boolean acquire(long time, TimeUnit unit) throws Exception {
        return delegate.acquire(time, unit);
    }

    @Override
    public void release() throws Exception {
        delegate.release();
    }

    @Override
    public void close() throws Exception {
        release();
    }
}
