package com.chord.framework.mybatis.runtime.statement.injector.program;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import com.chord.framework.mybatis.runtime.statement.injector.program.model.TableFieldDto;

import static com.baomidou.mybatisplus.core.toolkit.StringPool.*;

/**
 * Created on 2020/6/9
 *
 * @author: wulinfeng
 */
public class TableFieldMeta extends TableFieldDto {

    /**
     * 获取 insert 时候字段 sql 脚本片段
     * <p>insert into table (字段) values (值)</p>
     * <p>位于 "字段" 部位</p>
     *
     * <li> 根据规则会生成 if 标签 </li>
     *
     * @return sql 脚本片段
     */
    public String getInsertSqlColumnMaybeIf() {
        final String sqlScript = getInsertSqlColumn();
        if (withInsertFill) {
            return sqlScript;
        }
        return convertIf(sqlScript, property, insertStrategy);
    }

    public String getInsertSqlColumn() {
        return column + COMMA;
    }

    /**
     * 获取 insert 时候插入值 sql 脚本片段
     * <p>insert into table (字段) values (值)</p>
     * <p>位于 "值" 部位</p>
     *
     * <li> 根据规则会生成 if 标签 </li>
     *
     * @return sql 脚本片段
     */
    public String getInsertSqlPropertyMaybeIf(final String prefix) {
        String sqlScript = getInsertSqlProperty(prefix);
        if (withInsertFill) {
            return sqlScript;
        }
        return convertIf(sqlScript, property, insertStrategy);
    }

    /**
     * 获取 insert 时候插入值 sql 脚本片段
     * <p>insert into table (字段) values (值)</p>
     * <p>位于 "值" 部位</p>
     *
     * <li> 不生成 if 标签 </li>
     *
     * @return sql 脚本片段
     */
    public String getInsertSqlProperty(final String prefix) {
        final String newPrefix = prefix == null ? EMPTY : prefix;
        return SqlScriptUtils.safeParam(newPrefix + el) + COMMA;
    }

    /**
     * 转换成 if 标签的脚本片段
     *
     * @param sqlScript     sql 脚本片段
     * @param property      字段名
     * @param fieldStrategy 验证策略
     * @return if 脚本片段
     */
    private String convertIf(final String sqlScript, final String property, final FieldStrategy fieldStrategy) {
        if (fieldStrategy == FieldStrategy.NEVER) {
            return null;
        }
        if (fieldStrategy == FieldStrategy.IGNORED) {
            return sqlScript;
        }
        if (fieldStrategy == FieldStrategy.NOT_EMPTY && isCharSequence()) {
            return SqlScriptUtils.convertIf(sqlScript, String.format("%s != null and %s != ''", property, property),
                    false);
        }
        return SqlScriptUtils.convertIf(sqlScript, String.format("%s != null", property), false);
    }

    /**
     * 是否启用了逻辑删除
     */
    public boolean isLogicDelete() {
        return StringUtils.isNotBlank(logicDeleteValue) && StringUtils.isNotBlank(logicNotDeleteValue);
    }

    public String getSqlSelect(String propertyFormat, String prefix) {
        String sqlSelect = column;
        if(!StringUtils.isBlank(prefix)) {
            sqlSelect = prefix + "." + sqlSelect;
        }
        if (TableInfoHelper.checkRelated(true, this.property, this.column)) {
            String asProperty = this.property;
            if (StringUtils.isNotBlank(propertyFormat)) {
                asProperty = String.format(propertyFormat, this.property);
            }
            sqlSelect += (" AS " + asProperty);
        }
        return sqlSelect;
    }

    /**
     * 获取 查询的 sql 片段
     *
     * @param prefix 前缀
     * @return sql 脚本片段
     */
    public String getSqlWhere(final String prefix) {
        final String newPrefix = prefix == null ? EMPTY : prefix;
        // 默认:  AND column=#{prefix + el}
        String sqlScript = " AND " + String.format(condition, column, newPrefix + el);
        // 查询的时候只判非空
        return convertIf(sqlScript, convertIfProperty(newPrefix, property), whereStrategy);
    }

    private String convertIfProperty(String prefix, String property) {
        return StringUtils.isNotBlank(prefix) ? prefix.substring(0, prefix.length() - 1) + "['" + property + "']" : property;
    }

    /**
     * 获取 set sql 片段
     *
     * @param prefix 前缀
     * @return sql 脚本片段
     */
    public String getSqlSet(final String prefix) {
        return getSqlSet(false, prefix);
    }

    /**
     * 获取 set sql 片段
     *
     * @param ignoreIf 忽略 IF 包裹
     * @param prefix   前缀
     * @return sql 脚本片段
     */
    public String getSqlSet(final boolean ignoreIf, final String prefix) {
        final String newPrefix = prefix == null ? EMPTY : prefix;
        // 默认: column=
        String sqlSet = column + EQUALS;
        if (StringUtils.isNotBlank(update)) {
            sqlSet += String.format(update, column);
        } else {
            sqlSet += SqlScriptUtils.safeParam(newPrefix + el);
        }
        sqlSet += COMMA;
        if (ignoreIf) {
            return sqlSet;
        }
        if (withUpdateFill) {
            // 不进行 if 包裹
            return sqlSet;
        }
        return convertIf(sqlSet, convertIfProperty(prefix, property), updateStrategy);
    }

}
