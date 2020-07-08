package com.chord.framework.mybatis.runtime.statement.injector.program;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.core.incrementer.IKeyGenerator;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.ExceptionUtils;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import com.chord.framework.mybatis.runtime.statement.injector.program.model.TableMetaDto;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.SelectKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.baomidou.mybatisplus.core.toolkit.StringPool.*;
import static java.util.stream.Collectors.joining;

/**
 * Created on 2020/6/9
 *
 * @author: wulinfeng
 */
public class TableMeta extends TableMetaDto {

    private static final Logger logger = LoggerFactory.getLogger(TableMeta.class);

    private List<TableFieldMeta> fieldMetaList;

    private List<TableJoinMeta> joinMetaList;

    /**
     * 获取 insert 时候字段 sql 脚本片段
     * <p>insert into table (字段) values (值)</p>
     * <p>位于 "字段" 部位</p>
     *
     * <li> 自动选部位,根据规则会生成 if 标签 </li>
     *
     * @return sql 脚本片段
     */
    public String getAllInsertSqlColumnMaybeIf() {
        return getKeyInsertSqlColumn(true) + fieldMetaList.stream().map(TableFieldMeta::getInsertSqlColumnMaybeIf)
                .filter(Objects::nonNull).collect(joining(NEWLINE));
    }

    /**
     * 获取 insert 时候主键 sql 脚本片段
     * <p>insert into table (字段) values (值)</p>
     * <p>位于 "字段" 部位</p>
     *
     * @return sql 脚本片段
     */
    public String getKeyInsertSqlColumn(final boolean newLine) {
        if (StringUtils.isNotBlank(keyColumn)) {
            if (idType == IdType.AUTO) {
                return EMPTY;
            }
            return keyColumn + COMMA + (newLine ? NEWLINE : EMPTY);
        }
        return EMPTY;
    }

    /**
     * 获取所有 insert 时候插入值 sql 脚本片段
     * <p>insert into table (字段) values (值)</p>
     * <p>位于 "值" 部位</p>
     *
     * <li> 自动选部位,根据规则会生成 if 标签 </li>
     *
     * @return sql 脚本片段
     */
    public String getAllInsertSqlPropertyMaybeIf(final String prefix) {
        final String newPrefix = prefix == null ? EMPTY : prefix;
        return getKeyInsertSqlProperty(newPrefix, true) + fieldMetaList.stream()
                .map(i -> i.getInsertSqlPropertyMaybeIf(newPrefix)).filter(Objects::nonNull).collect(joining(NEWLINE));
    }

    /**
     * 获取 insert 时候主键 sql 脚本片段
     * <p>insert into table (字段) values (值)</p>
     * <p>位于 "值" 部位</p>
     *
     * @return sql 脚本片段
     */
    public String getKeyInsertSqlProperty(final String prefix, final boolean newLine) {
        final String newPrefix = prefix == null ? EMPTY : prefix;
        if (StringUtils.isNotBlank(keyProperty)) {
            if (idType == IdType.AUTO) {
                return EMPTY;
            }
            return SqlScriptUtils.safeParam(newPrefix + keyProperty) + COMMA + (newLine ? NEWLINE : EMPTY);
        }
        return EMPTY;
    }

    /**
     * 获取包含主键及字段的 select sql 片段
     *
     * @return sql 片段
     */
    public String getAllSqlSelect(String propertyFormat, String prefix) {
        return chooseSelect(TableFieldMeta::isSelect, propertyFormat, prefix);
    }

    /**
     * 获取需要进行查询的 select sql 片段
     *
     * @param predicate 过滤条件
     * @return sql 片段
     */
    public String chooseSelect(Predicate<TableFieldMeta> predicate, String propertyFormat, String prefix) {
        String sqlSelect = getKeySqlSelect(prefix);
        if(fieldList != null) {
            String fieldsSqlSelect = fieldMetaList.stream().filter(predicate)
                    .map(field->field.getSqlSelect(propertyFormat, prefix)).collect(joining(COMMA));
            if (StringUtils.isNotBlank(sqlSelect) && StringUtils.isNotBlank(fieldsSqlSelect)) {
                return sqlSelect + COMMA + fieldsSqlSelect;
            } else if (StringUtils.isNotBlank(fieldsSqlSelect)) {
                return fieldsSqlSelect;
            }
        }
        return sqlSelect;
    }

    /**
     * 获取主键的 select sql 片段
     *
     * @return sql 片段
     */
    public String getKeySqlSelect(String prefix) {
        String sqlSelect;
        if (StringUtils.isNotBlank(keyProperty)) {
            sqlSelect = keyColumn;
            if(!StringUtils.isBlank(prefix)) {
                sqlSelect = prefix + "." + sqlSelect;
            }
            if (keyRelated) {
                sqlSelect += (" AS " + keyProperty);
            }
        } else {
            sqlSelect = EMPTY;
        }
        return sqlSelect;
    }

    /**
     * 获取逻辑删除字段的 sql 脚本
     *
     * @param startWithAnd 是否以 and 开头
     * @param isWhere      是否需要的是逻辑删除值
     * @return sql 脚本
     */
    public String getLogicDeleteSql(boolean startWithAnd, boolean isWhere) {
        if (logicDelete) {
            TableFieldMeta field = fieldMetaList.stream().filter(TableFieldMeta::isLogicDelete).findFirst()
                    .orElseThrow(() -> ExceptionUtils.mpe("can't find the logicFiled from table {%s}", tableName));
            String logicDeleteSql = formatLogicDeleteSql(field, isWhere);
            if (startWithAnd) {
                logicDeleteSql = " AND " + logicDeleteSql;
            }
            return logicDeleteSql;
        }
        return EMPTY;
    }

    /**
     * format logic delete SQL, can be overrided by subclass
     *
     * @param field   TableFieldInfo
     * @param isWhere true: logicDeleteValue, false: logicNotDeleteValue
     * @return
     */
    private String formatLogicDeleteSql(TableFieldMeta field, boolean isWhere) {
        final String value = isWhere ? field.getLogicNotDeleteValue() : field.getLogicDeleteValue();
        if (isWhere) {
            if (NULL.equalsIgnoreCase(value)) {
                return field.getColumn() + " IS NULL";
            } else {
                return field.getColumn() + EQUALS + String.format(field.isCharSequence() ? "'%s'" : "%s", value);
            }
        }
        final String targetStr = field.getColumn() + EQUALS;
        if (NULL.equalsIgnoreCase(value)) {
            return targetStr + NULL;
        } else {
            return targetStr + String.format(field.isCharSequence() ? "'%s'" : "%s", value);
        }
    }

    /**
     * 获取所有的查询的 sql 片段
     *
     * @param ignoreLogicDelFiled 是否过滤掉逻辑删除字段
     * @param withId              是否包含 id 项
     * @param prefix              前缀
     * @return sql 脚本片段
     */
    public String getAllSqlWhere(boolean ignoreLogicDelFiled, boolean withId, final String prefix) {
        final String newPrefix = prefix == null ? EMPTY : prefix;
        String filedSqlScript = fieldMetaList.stream()
                .filter(i -> {
                    if (ignoreLogicDelFiled) {
                        return !(isLogicDelete() && i.isLogicDelete());
                    }
                    return true;
                })
                .map(i -> i.getSqlWhere(newPrefix)).filter(Objects::nonNull).collect(joining(NEWLINE));
        if (!withId || StringUtils.isBlank(keyProperty)) {
            return filedSqlScript;
        }
        String newKeyProperty = newPrefix + keyProperty;
        String keySqlScript = keyColumn + EQUALS + SqlScriptUtils.safeParam(newKeyProperty);
        return SqlScriptUtils.convertIf(keySqlScript, String.format("%s != null", newKeyProperty), false)
                + NEWLINE + filedSqlScript;
    }

    /**
     * 获取所有的 sql set 片段
     *
     * @param ignoreLogicDelFiled 是否过滤掉逻辑删除字段
     * @param prefix              前缀
     * @return sql 脚本片段
     */
    public String getAllSqlSet(boolean ignoreLogicDelFiled, final String prefix) {
        final String newPrefix = prefix == null ? EMPTY : prefix;
        return fieldMetaList.stream()
                .filter(i -> {
                    if (ignoreLogicDelFiled) {
                        return !(isLogicDelete() && i.isLogicDelete());
                    }
                    return true;
                }).map(i -> i.getSqlSet(newPrefix)).filter(Objects::nonNull).collect(joining(NEWLINE));
    }

    public List<TableFieldMeta> getFieldMetaList() {
        if(fieldMetaList == null) {
            if(fieldList != null) {
                fieldMetaList =
                        fieldMetaList.stream()
                                .map(dto -> {
                                    TableFieldMeta tableFieldMeta;
                                    try {
                                        tableFieldMeta = (TableFieldMeta) BeanUtils.cloneBean(dto);
                                    } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
                                        String errorMessage = "TableFieldMeta转换失败";
                                        logger.error(errorMessage, e);
                                        throw new RuntimeException(errorMessage);
                                    }
                                    return tableFieldMeta;
                                }).collect(Collectors.toList());
            }
        }
        return fieldMetaList;
    }

    public void setFieldMetaList(List<TableFieldMeta> fieldMetaList) {
        this.fieldMetaList = fieldMetaList;
    }

    public List<TableJoinMeta> getJoinMetaList() {
        if(joinMetaList == null) {
            if(joinList != null) {
                joinMetaList =
                        joinList.stream()
                                .map(dto -> {
                                    TableJoinMeta tableJoinMeta;
                                    try {
                                        tableJoinMeta = (TableJoinMeta) BeanUtils.cloneBean(dto);
                                    } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
                                        String errorMessage = "TableJoinMeta获取失败";
                                        logger.error(errorMessage, e);
                                        throw new RuntimeException(errorMessage);
                                    }
                                    return tableJoinMeta;
                                }).collect(Collectors.toList());
            }
        }
        return joinMetaList;
    }

    public void setJoinMetaList(List<TableJoinMeta> joinMetaList) {
        this.joinMetaList = joinMetaList;
    }

    public KeyGenerator genKeyGenerator(String baseStatementId, MapperBuilderAssistant builderAssistant) {
        IKeyGenerator keyGenerator = GlobalConfigUtils.getKeyGenerator(builderAssistant.getConfiguration());
        if (null == keyGenerator) {
            throw new IllegalArgumentException("not configure IKeyGenerator implementation class.");
        }
        Configuration configuration = builderAssistant.getConfiguration();
        //TODO 这里不加上builderAssistant.getCurrentNamespace()的会导致com.baomidou.mybatisplus.core.parser.SqlParserHelper.getSqlParserInfo越界
        String id = builderAssistant.getCurrentNamespace() + StringPool.DOT + baseStatementId + SelectKeyGenerator.SELECT_KEY_SUFFIX;
        ResultMap resultMap = new ResultMap.Builder(builderAssistant.getConfiguration(), id, getKeyType(), new ArrayList<>()).build();
        MappedStatement mappedStatement = new MappedStatement.Builder(builderAssistant.getConfiguration(), id,
                new StaticSqlSource(configuration, keyGenerator.executeSql(getKeySequence().value())), SqlCommandType.SELECT)
                .keyProperty(getKeyProperty())
                .resultMaps(Collections.singletonList(resultMap))
                .build();
        configuration.addMappedStatement(mappedStatement);
        return new SelectKeyGenerator(mappedStatement, true);
    }

}
