package com.chord.framework.mybatis.runtime.statement.injector.program;

import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import com.chord.framework.mybatis.runtime.utils.ReflectionUtils;
import com.google.common.base.Joiner;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created on 2020/6/2
 *
 * @author: wulinfeng
 */
public abstract class AbstractRuntimeMethod implements Constants {

    private static final Logger logger = LoggerFactory.getLogger(AbstractRuntimeMethod.class);

    protected Configuration configuration;
    protected LanguageDriver languageDriver;
    protected MapperBuilderAssistant builderAssistant;

    public void inject(MapperBuilderAssistant builderAssistant, TableMeta tableMeta, boolean changed) {
        this.configuration = builderAssistant.getConfiguration();
        this.builderAssistant = builderAssistant;
        this.languageDriver = configuration.getDefaultScriptingLanguageInstance();
        /* 注入自定义方法 */
        injectMappedStatement(tableMeta, changed);
    }

    public abstract MappedStatement injectMappedStatement(TableMeta tableMeta, boolean changed);

    /**
     * 是否已经存在MappedStatement
     *
     * @param mappedStatement MappedStatement
     * @return true or false
     */
    private boolean hasMappedStatement(String mappedStatement) {
        return configuration.hasStatement(mappedStatement, false);
    }

    /**
     * SQL 查询所有表字段
     *
     * @param tableMeta        表信息
     * @param queryWrapper 是否为使用 queryWrapper 查询
     * @return sql 脚本
     */
    protected String sqlSelectColumns(TableMeta tableMeta, boolean queryWrapper, String prefix) {
        /* 假设存在 resultMap 映射返回 */
        String selectColumns = ASTERISK;
        if (tableMeta.getResultMap() == null || (tableMeta.getResultMap() != null && tableMeta.isInitResultMap())) {
            GlobalConfig globalConfig = GlobalConfigUtils.getGlobalConfig(builderAssistant.getConfiguration());
            /* 普通查询，select * */
            selectColumns = tableMeta.getAllSqlSelect(globalConfig.getDbConfig().getPropertyFormat(), prefix);
        }
        if (!queryWrapper) {
            // 没有where条件
            return selectColumns;
        }
        // 传入参数"ew!=null and ew.selectSql!=null"，"${ew.selectSql}"，"*"
        // 转换成如下形式
        // <choose>
        //  <when test="ew!=null and ew.selectSql!=null">
//                 ${ew.selectSql}
        // </when>
        // <otherwise> * </otherwise>
        // </choose>
        return SqlScriptUtils.convertChoose(String.format("%s != null and %s != null", WRAPPER, Q_WRAPPER_SQL_SELECT),
                SqlScriptUtils.unSafeParam(Q_WRAPPER_SQL_SELECT), selectColumns);
    }

    /**
     *
     * SQL 查询所有表字段，包含join的表
     *
     * @param tableMeta
     * @param queryWrapper
     * @return
     */
    protected String sqlSelectJoinColumns(TableMeta tableMeta, boolean queryWrapper) {

        List<TableJoinMeta> joinList = tableMeta.getJoinMetaList();
        String selectColumns = Joiner.on(COMMA).join(
            joinList.stream()
                    .map(joinMeta -> joinMeta.getTableName() + DOT + ASTERISK)
                    .collect(Collectors.toList())
        );

        if (tableMeta.getResultMap() == null || (tableMeta.getResultMap() != null && tableMeta.isInitResultMap())) {
            GlobalConfig globalConfig = GlobalConfigUtils.getGlobalConfig(builderAssistant.getConfiguration());
            /* 普通查询，select * */
            List<String> columns = joinList.stream()
                    .map(joinMeta -> joinMeta.getAllSqlSelect(globalConfig.getDbConfig().getPropertyFormat(), joinMeta.getTableName()))
                    .collect(Collectors.toList());
            columns.add(tableMeta.getAllSqlSelect(globalConfig.getDbConfig().getPropertyFormat(), tableMeta.getTableName()));
            selectColumns = Joiner.on(COMMA).join(columns);
        }
        if (!queryWrapper) {
            // 没有where条件
            return selectColumns.toString();
        }

        return SqlScriptUtils.convertChoose(String.format("%s != null and %s != null", WRAPPER, Q_WRAPPER_SQL_SELECT),
                SqlScriptUtils.unSafeParam(Q_WRAPPER_SQL_SELECT), selectColumns.toString());

    }

    /**
     *
     * join的sql语句
     *
     * @param tableMeta
     * @return
     */
    protected String sqlJoin(TableMeta tableMeta) {
        return Joiner.on(NEWLINE).join(
            tableMeta.getJoinMetaList()
                    .stream()
                    .map(TableJoinMeta::getSqlJoin)
                    .collect(Collectors.toList())
        );
    }

    /**
     * 插入
     */
    protected MappedStatement addInsertMappedStatement(String namespace, String id,
                                                       SqlSource sqlSource, KeyGenerator keyGenerator,
                                                       String keyProperty, String keyColumn, boolean changed) {
        return addMappedStatement(namespace, id, sqlSource, SqlCommandType.INSERT, null,
                Integer.class, keyGenerator, keyProperty, keyColumn, changed);
    }

    /**
     * 查询
     */
    protected MappedStatement addSelectMappedStatementForTable(String namespace, String id, SqlSource sqlSource,
                                                               TableMeta table, boolean changed) {
        String resultMap = table.getResultMap();
        if (null != resultMap) {
            /* 返回 resultMap 映射结果集 */
            return addMappedStatement(namespace, id, sqlSource, SqlCommandType.SELECT, resultMap, null, new NoKeyGenerator(), null, null, changed);
        } else {
            /* 普通查询,返回Map */
            return addSelectMappedStatementForOther(namespace, id, sqlSource, Map.class, changed);
        }
    }

    /**
     * 查询
     */
    protected MappedStatement addSelectMappedStatementForOther(String namespace, String id, SqlSource sqlSource, Class<?> resultType, boolean changed) {
        return addMappedStatement(namespace, id, sqlSource, SqlCommandType.SELECT, null, resultType, new NoKeyGenerator(), null, null, changed);
    }

    /**
     * 更新
     */
    protected MappedStatement addUpdateMappedStatement(String namespace, String id,
                                                       SqlSource sqlSource, boolean changed) {
        return addMappedStatement(namespace, id, sqlSource, SqlCommandType.UPDATE,  null,
                Integer.class, new NoKeyGenerator(), null, null, changed);
    }

    /**
     * 删除
     */
    protected MappedStatement addDeleteMappedStatement(String namespace, String id, SqlSource sqlSource, boolean changed) {
        return addMappedStatement(namespace, id, sqlSource, SqlCommandType.DELETE,
                null, Integer.class, new NoKeyGenerator(), null, null, changed);
    }

    /**
     * 添加 MappedStatement 到 Mybatis 容器
     */
    protected MappedStatement addMappedStatement(String namespace, String id, SqlSource sqlSource,
                                                 SqlCommandType sqlCommandType,
                                                 String resultMap, Class<?> resultType, KeyGenerator keyGenerator,
                                                 String keyProperty, String keyColumn, boolean changed) {
        String statementName = namespace + DOT + id;
        if (hasMappedStatement(statementName)) {
            if(changed) {
                Field mappedStatementsField = ReflectionUtils.findField(configuration.getClass(), "mappedStatements");
                mappedStatementsField.setAccessible(true);
                Map<String, MappedStatement> statementMap = null;
                try {
                    statementMap = (Map<String, MappedStatement>) mappedStatementsField.get(configuration);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("动态获取sql出错");
                }
                statementMap.remove(statementName);
            } else {
                logger.warn(LEFT_SQ_BRACKET + statementName + "] Has been loaded by XML or SqlProvider or Mybatis's Annotation, so ignoring this injection for [" + getClass() + RIGHT_SQ_BRACKET);
                return null;
            }
        }

        boolean isSelect = false;
        if (sqlCommandType == SqlCommandType.SELECT) {
            isSelect = true;
        }
        return builderAssistant.addMappedStatement(id, sqlSource, StatementType.PREPARED, sqlCommandType,
                null, null, null, Map.class, resultMap, resultType,
                null, !isSelect, isSelect, false, keyGenerator, keyProperty, keyColumn,
                configuration.getDatabaseId(), languageDriver, null);
    }

    /**
     * SQL map 查询条件
     */
    protected String sqlWhereByMap(TableMeta table) {
        if (table.isLogicDelete()) {
            // 逻辑删除
            String sqlScript = SqlScriptUtils.convertChoose("v == null", " ${k} IS NULL ",
                    " ${k} = #{v} ");
            sqlScript = SqlScriptUtils.convertForeach(sqlScript, "cm", "k", "v", "AND");
            sqlScript = SqlScriptUtils.convertIf(sqlScript, "cm != null and !cm.isEmpty", true);
            sqlScript += (NEWLINE + table.getLogicDeleteSql(true, true));
            sqlScript = SqlScriptUtils.convertWhere(sqlScript);
            return sqlScript;
        } else {
            String sqlScript = SqlScriptUtils.convertChoose("v == null", " ${k} IS NULL ",
                    " ${k} = #{v} ");
            sqlScript = SqlScriptUtils.convertForeach(sqlScript, COLUMN_MAP, "k", "v", "AND");
            sqlScript = SqlScriptUtils.convertWhere(sqlScript);
            sqlScript = SqlScriptUtils.convertIf(sqlScript, String.format("%s != null and !%s", COLUMN_MAP,
                    COLUMN_MAP_IS_EMPTY), true);
            return sqlScript;
        }
    }

    /**
     * EntityWrapper方式获取select where
     *
     * @param newLine 是否提到下一行
     * @param table   表信息
     * @return String
     */
    protected String sqlWhereEntityWrapper(boolean newLine, TableMeta table) {
        if (table.isLogicDelete()) {
            String sqlScript = table.getAllSqlWhere(true, true, WRAPPER_ENTITY_DOT);
            sqlScript = SqlScriptUtils.convertIf(sqlScript, String.format("%s != null", WRAPPER_ENTITY),
                    true);
            sqlScript += (NEWLINE + table.getLogicDeleteSql(true, true) + NEWLINE);
            String normalSqlScript = SqlScriptUtils.convertIf(String.format("AND ${%s}", WRAPPER_SQLSEGMENT),
                    String.format("%s != null and %s != '' and %s", WRAPPER_SQLSEGMENT, WRAPPER_SQLSEGMENT,
                            WRAPPER_NONEMPTYOFNORMAL), true);
            normalSqlScript += NEWLINE;
            normalSqlScript += SqlScriptUtils.convertIf(String.format(" ${%s}", WRAPPER_SQLSEGMENT),
                    String.format("%s != null and %s != '' and %s", WRAPPER_SQLSEGMENT, WRAPPER_SQLSEGMENT,
                            WRAPPER_EMPTYOFNORMAL), true);
            sqlScript += normalSqlScript;
            sqlScript = SqlScriptUtils.convertChoose(String.format("%s != null", WRAPPER), sqlScript,
                    table.getLogicDeleteSql(false, true));
            sqlScript = SqlScriptUtils.convertWhere(sqlScript);
            return newLine ? NEWLINE + sqlScript : sqlScript;
        } else {
            String sqlScript = table.getAllSqlWhere(false, true, WRAPPER_ENTITY_DOT);
            sqlScript = SqlScriptUtils.convertIf(sqlScript, String.format("%s != null", WRAPPER_ENTITY), true);
            sqlScript += NEWLINE;
            sqlScript += SqlScriptUtils.convertIf(String.format(SqlScriptUtils.convertIf(" AND", String.format("%s and %s", WRAPPER_NONEMPTYOFENTITY, WRAPPER_NONEMPTYOFNORMAL), false) + " ${%s}", WRAPPER_SQLSEGMENT),
                    String.format("%s != null and %s != '' and %s", WRAPPER_SQLSEGMENT, WRAPPER_SQLSEGMENT,
                            WRAPPER_NONEMPTYOFWHERE), true);
            sqlScript = SqlScriptUtils.convertWhere(sqlScript) + NEWLINE;
            sqlScript += SqlScriptUtils.convertIf(String.format(" ${%s}", WRAPPER_SQLSEGMENT),
                    String.format("%s != null and %s != '' and %s", WRAPPER_SQLSEGMENT, WRAPPER_SQLSEGMENT,
                            WRAPPER_EMPTYOFWHERE), true);
            sqlScript = SqlScriptUtils.convertIf(sqlScript, String.format("%s != null", WRAPPER), true);
            return newLine ? NEWLINE + sqlScript : sqlScript;
        }
    }

    /**
     * SQL 更新 set 语句
     *
     * @param table 表信息
     * @return sql set 片段
     */
    protected String sqlLogicSet(TableMeta table) {
        return "SET " + table.getLogicDeleteSql(false, false);
    }

    /**
     * SQL 注释
     *
     * @return sql
     */
    protected String sqlComment() {
        return SqlScriptUtils.convertChoose(String.format("%s != null and %s != null", WRAPPER, Q_WRAPPER_SQL_COMMENT),
                SqlScriptUtils.unSafeParam(Q_WRAPPER_SQL_COMMENT), EMPTY);
    }

    protected String optlockVersion() {
        return "<if test=\"et instanceof java.util.Map\">" +
                " AND ${et." + Constants.MP_OPTLOCK_VERSION_COLUMN +
                "}=#{et." + Constants.MP_OPTLOCK_VERSION_ORIGINAL + StringPool.RIGHT_BRACE +
                "</if>";
    }

    /**
     * SQL 更新 set 语句
     *
     * @param logic  是否逻辑删除注入器
     * @param ew     是否存在 UpdateWrapper 条件
     * @param tableMeta  表信息
     * @param alias  别名
     * @param prefix 前缀
     * @return sql
     */
    protected String sqlSet(boolean logic, boolean ew, TableMeta tableMeta, boolean judgeAliasNull, String alias, String prefix) {
        String sqlScript = tableMeta.getAllSqlSet(logic, prefix);
        if (judgeAliasNull) {
            sqlScript = SqlScriptUtils.convertIf(sqlScript, String.format("%s != null", alias), true);
        }
        if (ew) {
            sqlScript += NEWLINE;
            sqlScript += SqlScriptUtils.convertIf(SqlScriptUtils.unSafeParam(U_WRAPPER_SQL_SET),
                    String.format("%s != null and %s != null", WRAPPER, U_WRAPPER_SQL_SET), false);
        }
        sqlScript = SqlScriptUtils.convertSet(sqlScript);
        return sqlScript;
    }

}
