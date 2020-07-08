package com.chord.framework.mybatis.runtime;

import java.util.function.Consumer;

/**
 *
 * sql语句相关信息生成
 *
 * Created on 2020/6/8
 *
 * @author: wulinfeng
 */
public interface StatementGenerator<T> {

    default T generate() {
        return generate((t)->{});
    }

    T generate(Consumer<T> callback);

}
