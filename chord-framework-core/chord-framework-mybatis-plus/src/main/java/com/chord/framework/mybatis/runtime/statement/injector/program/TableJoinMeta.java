package com.chord.framework.mybatis.runtime.statement.injector.program;

import com.chord.framework.mybatis.runtime.statement.injector.program.model.TableJoinDto;

import static com.baomidou.mybatisplus.core.toolkit.StringPool.ON;
import static com.baomidou.mybatisplus.core.toolkit.StringPool.SPACE;
import static com.chord.framework.mybatis.runtime.statement.injector.program.SqlInjectConstants.*;

/**
 * Created on 2020/6/9
 *
 * @author: wulinfeng
 */
public class TableJoinMeta extends TableJoinDto {

    /**
     *
     * 获得join表的查询列
     *
     * @param propertyFormat
     * @param prefix
     * @return
     */
    public String getAllSqlSelect(String propertyFormat, String prefix) {
        TableMeta tableMeta = new TableMeta();
        tableMeta.setTableName(super.getTableName());
        tableMeta.setFieldList(super.getFieldList());
        return tableMeta.getAllSqlSelect(propertyFormat, prefix);
    }

    /**
     *
     * 获得join语句
     *
     * @return
     */
    public String getSqlJoin() {
        String joinSql = "";
        switch(super.getJoinType()) {
            case JOIN: {
                joinSql = JOIN + SPACE + tableName + SPACE + ON + SPACE + condition;
                break;
            }
            case LEFT_JOIN: {
                joinSql = JOIN_LEFT + SPACE + tableName + SPACE + ON + SPACE + condition;
                break;
            }
            case RIGHT_JOIN: {
                joinSql = JOIN_RIGHT + SPACE + tableName + SPACE + ON + SPACE + condition;
                break;
            }
            default: break;
        }
        return joinSql;
    }

}
