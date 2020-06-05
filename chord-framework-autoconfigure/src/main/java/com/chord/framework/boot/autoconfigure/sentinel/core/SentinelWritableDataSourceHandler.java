package com.chord.framework.boot.autoconfigure.sentinel.core;

import com.alibaba.cloud.sentinel.SentinelProperties;
import com.alibaba.cloud.sentinel.datasource.config.AbstractDataSourceProperties;
import com.alibaba.cloud.sentinel.datasource.config.NacosDataSourceProperties;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.WritableDataSource;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.transport.util.WritableDataSourceRegistry;
import com.chord.framework.boot.autoconfigure.sentinel.common.ConsistencyModifyParamFlowRulesCommandHandler;
import com.chord.framework.boot.autoconfigure.sentinel.gateway.GatewayWritableDataSourceRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.env.Environment;

import java.util.List;

/**
 *
 * 复用SentinelProperties和DataSourceProperties，注册{@link NacosWritableDataSource|
 *
 * Created on 2020/5/14
 *
 * @author: wulinfeng
 */
public class SentinelWritableDataSourceHandler implements SmartInitializingSingleton {

    private static final Logger logger = LoggerFactory.getLogger(SentinelWritableDataSourceHandler.class);

    private final DefaultListableBeanFactory beanFactory;

    private final SentinelProperties sentinelProperties;

    private final Environment env;

    public SentinelWritableDataSourceHandler(DefaultListableBeanFactory beanFactory,
                                             SentinelProperties sentinelProperties, Environment env) {
        this.beanFactory = beanFactory;
        this.sentinelProperties = sentinelProperties;
        this.env = env;
    }

    @Override
    public void afterSingletonsInstantiated() {
        sentinelProperties.getDatasource()
                .forEach((dataSourceName, dataSourceProperties) -> {
                    try {
                        List<String> validFields = dataSourceProperties.getValidField();
                        if (validFields.size() != 1) {
                            logger.error("[Sentinel Starter] DataSource " + dataSourceName
                                    + " multi datasource active and won't loaded: "
                                    + dataSourceProperties.getValidField());
                            return;
                        }
                        AbstractDataSourceProperties abstractDataSourceProperties = dataSourceProperties
                                .getValidDataSourceProperties();
                        abstractDataSourceProperties.setEnv(env);
                        abstractDataSourceProperties.preCheck(dataSourceName);
                        register(abstractDataSourceProperties);
                    }
                    catch (Exception e) {
                        logger.error("[Sentinel Starter] DataSource " + dataSourceName
                                + " build error: " + e.getMessage(), e);
                    }
                });
    }

    private void register(final AbstractDataSourceProperties dataSourceProperties) {
        switch (dataSourceProperties.getRuleType()) {
            case FLOW:
                if(dataSourceProperties instanceof NacosDataSourceProperties) {
                    WritableDataSource<List<FlowRule>> wds = createWritableDataSource((NacosDataSourceProperties) dataSourceProperties);
                    WritableDataSourceRegistry.registerFlowDataSource(wds);
                } else {
                    logger.warn("no writable data source for the data source");
                }
                break;
            case DEGRADE:
                if(dataSourceProperties instanceof NacosDataSourceProperties) {
                    WritableDataSource<List<DegradeRule>> wds = createWritableDataSource((NacosDataSourceProperties) dataSourceProperties);
                    WritableDataSourceRegistry.registerDegradeDataSource(wds);
                } else {
                    logger.warn("no writable data source for the data source");
                }
                break;
            case PARAM_FLOW:
                if(dataSourceProperties instanceof NacosDataSourceProperties) {
                    WritableDataSource<List<ParamFlowRule>> wds = createWritableDataSource((NacosDataSourceProperties) dataSourceProperties);
                    ConsistencyModifyParamFlowRulesCommandHandler.setWritableDataSource(wds);
                } else {
                    logger.warn("no writable data source for the data source");
                }
                break;
            case SYSTEM:
                if(dataSourceProperties instanceof NacosDataSourceProperties) {
                    WritableDataSource<List<SystemRule>> wds = createWritableDataSource((NacosDataSourceProperties) dataSourceProperties);
                    WritableDataSourceRegistry.registerSystemDataSource(wds);
                } else {
                    logger.warn("no writable data source for the data source");
                }
                break;
            case AUTHORITY:
                if(dataSourceProperties instanceof NacosDataSourceProperties) {
                    WritableDataSource<List<AuthorityRule>> wds = createWritableDataSource((NacosDataSourceProperties) dataSourceProperties);
                    WritableDataSourceRegistry.registerAuthorityDataSource(wds);
                } else {
                    logger.warn("no writable data source for the data source");
                }
                break;
            case GW_FLOW:
                if(dataSourceProperties instanceof NacosDataSourceProperties) {
                    WritableDataSource<List<GatewayFlowRule>> wds = createWritableDataSource((NacosDataSourceProperties) dataSourceProperties);
                    GatewayWritableDataSourceRegistry.registerGatewayFlowDataSource(wds);
                } else {
                    logger.warn("no writable data source for the data source");
                }
                break;
            case GW_API_GROUP:
                if(dataSourceProperties instanceof NacosDataSourceProperties) {
                    WritableDataSource<String> wds = createSingleWritableDataSource((NacosDataSourceProperties) dataSourceProperties);
                    GatewayWritableDataSourceRegistry.registerGatewayApiDefinitionGroupDataSource(wds);
                } else {
                    logger.warn("no writable data source for the data source");
                }
                break;
            default:
                break;
        }
    }

    private <T> WritableDataSource<List<T>> createWritableDataSource(NacosDataSourceProperties nacosDataSourceProperties) {
        Converter<List<T>, String> converter = (source) -> {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                return objectMapper.writeValueAsString(source);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("cannot converter the value");
            }
        };
        return new NacosWritableDataSource<>(
                nacosDataSourceProperties.getServerAddr(),
                nacosDataSourceProperties.getGroupId(),
                nacosDataSourceProperties.getDataId(),
                converter);
    }

    private <T> WritableDataSource<T> createSingleWritableDataSource(NacosDataSourceProperties nacosDataSourceProperties) {
        Converter<T, String> converter = (source) -> {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                return objectMapper.writeValueAsString(source);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("cannot converter the value");
            }
        };
        return new NacosWritableDataSource<>(
                nacosDataSourceProperties.getServerAddr(),
                nacosDataSourceProperties.getGroupId(),
                nacosDataSourceProperties.getDataId(),
                converter);
    }

}
