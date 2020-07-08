package com.chord.framework.mybatis.runtime;

import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;

import java.util.Map;

/**
 * Created on 2020/6/12
 *
 * @author: wulinfeng
 */
public interface KeyResolver<T> {

    KeyGenerator getKeyGenerator();

    String getKeyProperty(T model);

    String getKeyColumn(T model);

    class DefaultKeyResolver implements KeyResolver<Map<String, Object>> {

        public static final String KEY_PROPERTY = "keyProperty";
        public static final String KEY_COLUMN = "keyColumn";

        @Override
        public KeyGenerator getKeyGenerator() {
            return new NoKeyGenerator();
        }

        @Override
        public String getKeyProperty(Map<String, Object> model) {
            if(model.get(KEY_PROPERTY) instanceof String) {
                return (String) model.get(KEY_PROPERTY);
            }
            return null;
        }

        @Override
        public String getKeyColumn(Map<String, Object> model) {
            if(model.get(KEY_COLUMN) instanceof String) {
                return (String) model.get(KEY_COLUMN);
            }
            return null;
        }

    }

}
