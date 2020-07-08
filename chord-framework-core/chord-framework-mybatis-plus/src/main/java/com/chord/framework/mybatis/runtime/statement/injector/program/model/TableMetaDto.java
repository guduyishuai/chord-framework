package com.chord.framework.mybatis.runtime.statement.injector.program.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Created on 2020/6/9
 *
 * @author: wulinfeng
 */
@Data
public class TableMetaDto {

    @NotEmpty(message = "命名空间不能为空")
    protected String namespace;

    protected IdType idType;

    /**
     * 主键属性名
     */
    protected String keyProperty;

    /**
     * 主键列名
     */
    protected String keyColumn;

    /**
     * 表主键ID 属性类型
     */
    private Class<?> keyType;

    /**
     * 表主键ID Sequence
     */
    private KeySequence keySequence;

    @NotEmpty(message = "表明不能为空")
    protected String tableName;

    protected List<TableFieldDto> fieldList;

    protected List<TableJoinDto> joinList;

    /**
     * 表映射结果集，比如需要查询的字段，如果为空，则查询*
     */
    protected String resultMap;

    /**
     * 是否是自动生成的 resultMap，如果reaultMap不为空，并且initResultMap为true，则查询*
     */
    protected boolean initResultMap;

    /**
     * 主键是否有存在字段名与属性名关联
     * <p>true: 表示要进行 as</p>
     */
    protected boolean keyRelated;

    /**
     * 是否开启逻辑删除
     */
    protected boolean logicDelete = false;

    /**
     * 数据库类型
     */
    protected String databaseId;

}
