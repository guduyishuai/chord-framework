package com.chord.framework.mutex.lock;

import java.util.concurrent.TimeUnit;

/**
 * Created on 2020/6/23
 *
 * @author: wulinfeng
 */
public interface Lock extends AutoCloseable {

    /**
     *
     * 获得锁的名称
     *
     * @return
     */
    String getName();

    /**
     *
     * 获取锁
     *
     * @throws Exception
     */
    void acquire() throws Exception;

    /**
     *
     * 获取锁，如果超时则抛出异常
     *
     * @param time
     * @param unit
     * @return
     * @throws Exception
     */
    boolean acquire(long time, TimeUnit unit) throws Exception;

    /**
     *
     * 释放锁
     *
     * @throws Exception
     */
    void release() throws Exception;

}
