package com.chord.framework.boot.autoconfigure.sentinel.core;

import com.alibaba.csp.sentinel.datasource.WritableDataSource;
import com.alibaba.csp.sentinel.log.RecordLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created on 2020/5/20
 *
 * @author: wulinfeng
 */
public class WriteableDataSourceUtils {

    private static final Logger logger = LoggerFactory.getLogger(WriteableDataSourceUtils.class);

    public static <T> boolean writeToDataSource(WritableDataSource<T> dataSource, T value) {
        if (dataSource != null) {
            try {
                dataSource.write(value);
            } catch (Exception e) {
                RecordLog.warn("Write data source failed", e);
                logger.error("Write data source failed", e);
                return false;
            }
        }
        return true;
    }

}
