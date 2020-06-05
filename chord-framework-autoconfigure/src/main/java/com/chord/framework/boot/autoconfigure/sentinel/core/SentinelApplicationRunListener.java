package com.chord.framework.boot.autoconfigure.sentinel.core;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.chord.framework.boot.autoconfigure.common.ContextUtils;
import com.chord.framework.boot.autoconfigure.sentinel.gateway.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.ClassUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 *
 * starting阶段的设置
 * 设置SentinelConfig
 *
 * started阶段的设置
 * 根据配置设置api分组
 *
 * Created on 2020/5/19
 *
 * @author: wulinfeng
 */
public class SentinelApplicationRunListener implements SpringApplicationRunListener {

    private final static Logger logger = LoggerFactory.getLogger(SentinelApplicationRunListener.class);

    private String[] args;

    public SentinelApplicationRunListener(SpringApplication application, String[] args){
        this.args = args;
    }

    @Override
    public void starting() {
        try {

            ClassUtils.getDefaultClassLoader().loadClass(SentinelGatewayFilter.class.getName());
            if(!Stream.of(this.args).anyMatch(arg->arg.contains(SentinelConfig.APP_TYPE))) {
                logger.debug("gateway app type");
                System.setProperty(SentinelConfig.APP_TYPE, String.valueOf(SentinelGatewayConstants.APP_TYPE_GATEWAY));
            }

        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            logger.debug("common app type");
        }

    }

    @Override
    public void environmentPrepared(ConfigurableEnvironment environment) {
        logger.debug("environmentPrepared");
        logger.debug("environmentPrepared from bootstrap: " + ContextUtils.isBootstap(environment));
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        logger.debug("contextPrepared");
        logger.debug("contextPrepared from bootstrap: " + ContextUtils.isBootstap(context.getEnvironment()));
    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        logger.debug("contextLoaded");
        logger.debug("contextLoaded from bootstrap: " + ContextUtils.isBootstap(context.getEnvironment()));
    }

    @Override
    public void started(ConfigurableApplicationContext context) {
        logger.debug("started");
        logger.debug("started from bootstrap: " + ContextUtils.isBootstap(context.getEnvironment()));
        try {

            ClassUtils.getDefaultClassLoader().loadClass(SentinelGatewayFilter.class.getName());
            logger.debug("gateway app type");

            ApiProperties apiProperties = new ApiProperties();

            if(ContextUtils.isBootstap(context.getEnvironment())) {
                // 处理bootstrap中的配置
                ContextUtils.bindProperty(context.getEnvironment(), ApiProperties.PREFIX, apiProperties);
            } else {
                // 处理application中的配置
                // 如果ApiProperties找不到会抛出NoSuchBeanDefinitionException
                apiProperties = context.getBean(ApiProperties.class);
            }

            Map<String, ApiDefinitionProperties> apis = apiProperties.getDefinition();
            if(apis == null) {
                return;
            }

            Set<ApiDefinition> definitions = new HashSet<>();
            apis.forEach((name, definitioin) -> {
                ApiDefinition apiDefinition = new ApiDefinition(name);
                Set<ApiPredicateItem> apiPredicateItems = new HashSet<>();
                Set<ApiPredicateItemProperties> items = definitioin.getItem();
                for(ApiPredicateItemProperties item : items) {
                    UrlMatchStategy stategy = item.getStategy();
                    if(stategy == null) {
                        throw new IllegalArgumentException(ApiProperties.PREFIX + "." + name + "配置的stategy不支持");
                    }
                    ApiPredicateItem apiPredicateItem = new ApiPathPredicateItem();
                    ((ApiPathPredicateItem) apiPredicateItem).setPattern(item.getPattern());
                    ((ApiPathPredicateItem) apiPredicateItem).setMatchStrategy(stategy.getStrategy());
                    apiPredicateItems.add(apiPredicateItem);
                }
                apiDefinition.setPredicateItems(apiPredicateItems);
                definitions.add(apiDefinition);
            });

            if(apiProperties.isEnableNacos()) {
                ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                String apiDefinitionJson = objectMapper.writeValueAsString(definitions);
                if(!WriteableDataSourceUtils.writeToDataSource(
                        GatewayWritableDataSourceRegistry.getGatewayApiDefinitionGroupDataSource(), apiDefinitionJson)) {
                    throw new RuntimeException("ApiDefinition同步nacos失败");
                }
            }

            GatewayApiDefinitionManager.loadApiDefinitions(definitions);

        } catch (ClassNotFoundException | NoClassDefFoundError e) {

            logger.debug("common app type");

        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("序列化ApiDefinition失败，请检查配置");
        }
    }

    @Override
    public void running(ConfigurableApplicationContext context) {
        logger.debug("running");
        logger.debug("running from bootstrap: " + ContextUtils.isBootstap(context.getEnvironment()));
    }

    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {
        logger.debug("failed");
    }

}
