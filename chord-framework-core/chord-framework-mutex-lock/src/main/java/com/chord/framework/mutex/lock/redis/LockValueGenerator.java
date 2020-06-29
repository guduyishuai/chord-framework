package com.chord.framework.mutex.lock.redis;

import java.io.Serializable;

/**
 * Created on 2020/6/24
 *
 * @author: wulinfeng
 */
public interface LockValueGenerator {

    /**
     *
     * 生成锁对应的值，比如redis的value
     *
     * @return
     */
    Serializable generate();

}
