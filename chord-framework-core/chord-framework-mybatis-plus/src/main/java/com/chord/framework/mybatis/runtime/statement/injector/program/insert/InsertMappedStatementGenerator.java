package com.chord.framework.mybatis.runtime.statement.injector.program.insert;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import com.chord.framework.mybatis.runtime.MappedStatementInfo;
import com.chord.framework.mybatis.runtime.StatementGenerator;
import com.chord.framework.mybatis.runtime.statement.injector.program.TableMeta;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Consumer;

import static com.baomidou.mybatisplus.core.toolkit.StringPool.*;

/**
 * Created on 2020/6/9
 *
 * @author: wulinfeng
 */
public class InsertMappedStatementGenerator implements StatementGenerator<MappedStatementInfo> {

    private static final Logger logger = LoggerFactory.getLogger(InsertMappedStatementGenerator.class);

    private TableMeta tableMeta;

    private Configuration configuration;

    private LanguageDriver languageDriver;

    private MapperBuilderAssistant builderAssistant;

    public InsertMappedStatementGenerator(TableMeta tableMeta,
                                          Configuration configuration,
                                          LanguageDriver languageDriver,
                                          MapperBuilderAssistant builderAssistant) {
        this.tableMeta = tableMeta;
        this.configuration = configuration;
        this.languageDriver = languageDriver;
        this.builderAssistant = builderAssistant;
    }

    @Override
    public MappedStatementInfo generate(Consumer<MappedStatementInfo> callback) {

        // 不使用key生成器
        KeyGenerator keyGenerator = new NoKeyGenerator();
        // mybatis-plus默认sql类型
        SqlMethod sqlMethod = SqlMethod.INSERT_ONE;

        // 生成insert的列sql和values的sql
        String columnScript = SqlScriptUtils.convertTrim(tableMeta.getAllInsertSqlColumnMaybeIf(),
                LEFT_BRACKET, RIGHT_BRACKET, null, COMMA);
        String valuesScript = SqlScriptUtils.convertTrim(tableMeta.getAllInsertSqlPropertyMaybeIf(null),
                LEFT_BRACKET, RIGHT_BRACKET, null, COMMA);

        // 处理主键信息
        String keyProperty = null;
        String keyColumn = null;
        // 表包含主键处理逻辑,如果不包含主键当普通字段处理
        if (StringUtils.isNotBlank(tableMeta.getKeyProperty())) {
            if (tableMeta.getIdType() == IdType.AUTO) {
                /** 自增主键 */
                keyGenerator = new Jdbc3KeyGenerator();
                keyProperty = tableMeta.getKeyProperty();
                keyColumn = tableMeta.getKeyColumn();
            } else {
                if (null != tableMeta.getKeySequence()) {
                    keyGenerator = tableMeta.genKeyGenerator(sqlMethod.getMethod(), builderAssistant);
                    keyProperty = tableMeta.getKeyProperty();
                    keyColumn = tableMeta.getKeyColumn();
                }
            }
        }

        String sql = String.format(sqlMethod.getSql(), tableMeta.getTableName(), columnScript, valuesScript);
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, Map.class);
        logger.info("create mappedStatement {}: \n {}", tableMeta.getNamespace() + "." + sqlMethod.getMethod(), sql);

        MappedStatementInfo mappedStatementInfo = new MappedStatementInfo(tableMeta.getNamespace(), sqlMethod.getMethod(), sqlSource, keyGenerator, keyProperty, keyColumn, tableMeta.getDatabaseId());

        callback.accept(mappedStatementInfo);

        return mappedStatementInfo;

    }

}
