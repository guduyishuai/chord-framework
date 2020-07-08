package com.chord.framework.mybatis.runtime.statement.injector.program.model;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.SqlCondition;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * Created on 2020/6/9
 *
 * @author: wulinfeng
 */
@Data
public class TableFieldDto {

    /**
     * 列名
     */
    @NotEmpty(message = "列名不能为空")
    protected String column;

    /**
     * 属性名，对应传入参数
     */
    @NotEmpty(message = "属性名不能为空")
    protected String property;

    /**
     * #{property}表达式
     */
    protected String el;

    /**
     * 表字段是否启用了插入填充
     */
    protected boolean withInsertFill;

    protected FieldStrategy insertStrategy;

    /**
     * 是否是字符串
     */
    protected boolean charSequence;

    /**
     * 是否进行 select 查询
     * <p>大字段可设置为 false 不加入 select 查询范围</p>
     */
    protected boolean select = true;

    /**
     * 逻辑删除值
     */
    protected String logicDeleteValue;
    /**
     * 逻辑未删除值
     */
    protected String logicNotDeleteValue;

    /**
     * where 字段比较条件
     */
    protected String condition = SqlCondition.EQUAL;

    /**
     * 字段验证策略之 where
     */
    protected final FieldStrategy whereStrategy = FieldStrategy.DEFAULT;

    /**
     * 字段 update set 部分注入
     */
    protected String update;

    /**
     * 表字段是否启用了更新填充
     */
    protected boolean withUpdateFill;

    /**
     * 字段验证策略之 update
     */
    protected final FieldStrategy updateStrategy = FieldStrategy.DEFAULT;

}
