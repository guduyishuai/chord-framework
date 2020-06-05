package com.chord.framework.boot.autoconfigure.sentinel.core;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.WritableDataSource;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 *
 * 配置规则同步Nacos
 *
 * Created on 2020/5/14
 *
 * @author: wulinfeng
 */
public class NacosWritableDataSource<T> implements WritableDataSource<T> {

    private static final Logger logger = LoggerFactory.getLogger(NacosWritableDataSource.class);

    private final String groupId;
    private final String dataId;
    private final Properties properties;

    private Converter<T, String> parser;

    /**
     * Note: The Nacos config might be null if its initialization failed.
     */
    private ConfigService configService = null;

    public NacosWritableDataSource(final String serverAddr, final String groupId, final String dataId,
                                   Converter<T, String> parser) {
        this(NacosWritableDataSource.buildProperties(serverAddr), groupId, dataId, parser);
    }

    public NacosWritableDataSource(final Properties properties, final String groupId, final String dataId,
                                   Converter<T, String> parser) {
        this.parser = parser;
        if (StringUtil.isBlank(groupId) || StringUtil.isBlank(dataId)) {
            throw new IllegalArgumentException(String.format("Bad argument: groupId=[%s], dataId=[%s]",
                    groupId, dataId));
        }
        AssertUtil.notNull(properties, "Nacos properties must not be null, you could put some keys from PropertyKeyConst");
        this.groupId = groupId;
        this.dataId = dataId;
        this.properties = properties;
        initNacos();
    }

    private void initNacos() {
        try {
            this.configService = NacosFactory.createConfigService(this.properties);
        } catch (Exception e) {
            RecordLog.warn("[NacosDataSource] Error occurred when initializing Nacos data source", e);
            e.printStackTrace();
        }
    }

    @Override
    public void write(T value) throws Exception {
        if (configService == null) {
            throw new IllegalStateException("Nacos config service has not been initialized or error occurred");
        }
        String content;
        if(value instanceof String) {
            content = (String) value;
        } else {
            content = this.parser.convert(value);
        }
        boolean isSuccess = configService.publishConfig(this.dataId, this.groupId, content);
        if(!isSuccess) {
            throw new RuntimeException("write nacos failed");
        }
    }

    @Override
    public void close() throws Exception {

    }

    private static Properties buildProperties(String serverAddr) {
        Properties properties = new Properties();
        properties.setProperty(PropertyKeyConst.SERVER_ADDR, serverAddr);
        return properties;
    }

}
