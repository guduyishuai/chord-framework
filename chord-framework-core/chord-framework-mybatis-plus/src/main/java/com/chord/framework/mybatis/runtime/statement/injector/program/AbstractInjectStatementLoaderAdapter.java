package com.chord.framework.mybatis.runtime.statement.injector.program;

import com.chord.framework.mybatis.runtime.AbstractStatementLoader;
import com.chord.framework.mybatis.runtime.CacheCleaner;
import org.apache.ibatis.builder.MapperBuilderAssistant;

/**
 * Created on 2020/6/10
 *
 * @author: wulinfeng
 */
public class AbstractInjectStatementLoaderAdapter extends AbstractStatementLoader {

    private final AbstractRuntimeMethod runtimeMethod;

    private final MapperBuilderAssistant builderAssistant;

    private final TableMeta tableMeta;

    public AbstractInjectStatementLoaderAdapter(AbstractRuntimeMethod runtimeMethod,
                                                CacheCleaner cacheCleaner,
                                                MapperBuilderAssistant builderAssistant,
                                                TableMeta tableMeta) {
        super(cacheCleaner);
        this.runtimeMethod = runtimeMethod;
        this.builderAssistant = builderAssistant;
        this.tableMeta = tableMeta;
    }

    @Override
    public boolean doLoad() {
        runtimeMethod.inject(builderAssistant, tableMeta, true);
        return true;
    }
}
