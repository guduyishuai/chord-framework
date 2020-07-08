package com.chord.framework.mybatis.runtime.statement.injector.template.insert;

import com.chord.framework.mybatis.runtime.MappedStatementInfo;
import com.chord.framework.mybatis.runtime.StatementGenerator;

import java.util.function.Consumer;

/**
 * Created on 2020/6/15
 *
 * @author: wulinfeng
 */
public class CustomInsertTemplateMappedStatementGenerator implements StatementGenerator<MappedStatementInfo> {

    @Override
    public MappedStatementInfo generate(Consumer<MappedStatementInfo> callback) {
        return null;
    }

}
