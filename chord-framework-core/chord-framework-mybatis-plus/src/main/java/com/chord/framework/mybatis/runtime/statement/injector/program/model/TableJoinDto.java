package com.chord.framework.mybatis.runtime.statement.injector.program.model;

import com.chord.framework.mybatis.runtime.statement.injector.program.JoinType;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Created on 2020/6/9
 *
 * @author: wulinfeng
 */
@Data
public class TableJoinDto {

    /**
     * 表名
     */
    @NotEmpty(message = "表明不能为空")
    protected String tableName;

    /**
     * 字段列表不能为空
     */
    @NotEmpty(message = "字段列表不能为空")
    protected List<TableFieldDto> fieldList;

    /**
     * join类型
     */
    protected JoinType joinType;

    /**
     * join条件
     */
    @NotEmpty(message = "join条件不能为空")
    protected String condition;

}
