package com.chord.framework.mutex.lock;

/**
 * Created on 2020/6/23
 *
 * @author: wulinfeng
 */
public interface LockFactory {

    /**
     *
     * 创建锁，锁的名称为默认名称
     *
     * @return
     */
    default Lock create() {
        return create(AbstractLock.DEFAULT_NAME);
    }

    /**
     *
     * 创建锁
     *
     * @param name
     * @return
     */
    Lock create(String name);

}
