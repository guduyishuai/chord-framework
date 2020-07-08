package com.chord.framework.mybatis.runtime.statement.injector.program.insert;

import com.chord.framework.mybatis.runtime.MappedStatementInfo;
import com.chord.framework.mybatis.runtime.statement.injector.program.AbstractRuntimeMethod;
import com.chord.framework.mybatis.runtime.statement.injector.program.TableMeta;
import org.apache.ibatis.mapping.MappedStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created on 2020/6/2
 *
 * @author: wulinfeng
 */
public class RuntimeInsert extends AbstractRuntimeMethod {

    private static final Logger logger = LoggerFactory.getLogger(RuntimeInsert.class);

    @Override
    public MappedStatement injectMappedStatement(TableMeta tableMeta, boolean changed) {

        MappedStatementInfo mappedStatementInfo = new InsertMappedStatementGenerator(tableMeta, configuration, languageDriver, builderAssistant).generate();
        return this.addInsertMappedStatement(
                tableMeta.getNamespace(),
                mappedStatementInfo.getId(),
                mappedStatementInfo.getSqlSource(),
                mappedStatementInfo.getKeyGenerator(),
                tableMeta.getKeyProperty(),
                tableMeta.getKeyColumn(),
                changed);

    }

}
