package com.chord.framework.mutex.lock;

/**
 * Created on 2020/6/23
 *
 * @author: wulinfeng
 */
public abstract class AbstractLock<T> implements Lock {

    public static final String DEFAULT_NAME = "CHORD_LOCK";

    protected final T delegate;

    protected final String name;

    public AbstractLock(T delegate) {
        this.delegate = delegate;
        this.name = DEFAULT_NAME;
    }

    public AbstractLock(T delegate, String name) {
        this.delegate = delegate;
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void close() throws Exception {
        release();
    }

}
