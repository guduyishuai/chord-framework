package com.chord.framework.mybatis.runtime.statement.injector.template.insert;

import lombok.Data;

/**
 * Created on 2020/6/15
 *
 * @author: wulinfeng
 */
@Data
public class InsertModel {

    private String tableName;

    private String column;

    private String property;

}
