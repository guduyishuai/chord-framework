package com.chord.framework.mybatis.runtime;

/**
 * Created on 2020/6/8
 *
 * @author: wulinfeng
 */
public abstract class AbstractStatementLoader implements StatementLoader {

    protected final CacheCleaner cacheCleaner;

    public AbstractStatementLoader(CacheCleaner cacheCleaner) {
        this.cacheCleaner = cacheCleaner;
    }

    @Override
    public boolean load() {
        cacheCleaner.clean();
        return doLoad();
    }

    public abstract boolean doLoad();

}
