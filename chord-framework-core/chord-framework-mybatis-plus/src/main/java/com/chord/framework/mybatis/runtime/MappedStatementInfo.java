package com.chord.framework.mybatis.runtime;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.Data;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.mapping.SqlSource;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Created on 2020/6/8
 *
 * @author: wulinfeng
 */
@Data
public class MappedStatementInfo {

    public MappedStatementInfo(String namespace, String id, SqlSource sqlSource,
                               KeyGenerator keyGenerator,
                               String keyProperty, String keyColumn,
                               String databaseId) {
        if(StringUtils.isBlank(namespace)) {
            throw new RuntimeException("生成mappedStatement异常，命名空间不能为空");
        }
        if(StringUtils.isBlank(id)) {
            throw new RuntimeException("生成mappedStatement异常，id不能为空");
        }
        if(sqlSource == null) {
            throw new RuntimeException("生成mappedStatement异常，sqlSource不能为空");
        }
        if(StringUtils.isBlank(keyProperty)) {
            throw new RuntimeException("生成mappedStatement异常，主键属性名不能为空");
        }
        if(StringUtils.isBlank(keyColumn)) {
            throw new RuntimeException("生成mappedStatement异常，主键列名不能为空");
        }
        this.namespace = namespace;
        this.id = id;
        this.sqlSource = sqlSource;
        this.keyGenerator = keyGenerator;
        this.keyProperty = keyProperty;
        this.keyColumn = keyColumn;
        this.databaseId = databaseId;
    }

    @NotEmpty
    private String namespace;

    @NotEmpty
    private String id;

    @NotNull
    private SqlSource sqlSource;

    private KeyGenerator keyGenerator;

    @NotNull
    private String keyProperty;

    @NotNull
    private String keyColumn;

    @NotNull
    private String databaseId;

}
