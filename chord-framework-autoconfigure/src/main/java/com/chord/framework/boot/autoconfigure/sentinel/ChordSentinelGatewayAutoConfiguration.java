package com.chord.framework.boot.autoconfigure.sentinel;

import com.alibaba.cloud.sentinel.datasource.converter.JsonConverter;
import com.alibaba.cloud.sentinel.datasource.converter.SentinelConverter;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter;
import com.alibaba.csp.sentinel.adapter.gateway.sc.exception.SentinelGatewayBlockExceptionHandler;
import com.chord.framework.sentinel.properties.gateway.ApiProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.result.view.ViewResolver;

import java.io.IOException;
import java.util.*;

/**
 * Created on 2020/5/19
 *
 * @author: wulinfeng
 */
@ConditionalOnClass(SentinelGatewayFilter.class)
@EnableConfigurationProperties(ApiProperties.class)
@Configuration
public class ChordSentinelGatewayAutoConfiguration {

    private final List<ViewResolver> viewResolvers;
    private final ServerCodecConfigurer serverCodecConfigurer;
    private final ObjectMapper objectMapper;

    public ChordSentinelGatewayAutoConfiguration(ObjectProvider<List<ViewResolver>> viewResolversProvider,
                                ObjectProvider<ServerCodecConfigurer> serverCodecConfigurerProvider) {
        this.viewResolvers = viewResolversProvider.getIfAvailable(Collections::emptyList);
        this.serverCodecConfigurer = serverCodecConfigurerProvider.getIfAvailable(ServerCodecConfigurer::create);
        this.objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @ConditionalOnMissingBean
    public SentinelGatewayBlockExceptionHandler sentinelGatewayBlockExceptionHandler() {
        return new SentinelGatewayBlockExceptionHandler(viewResolvers, serverCodecConfigurer);
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public GlobalFilter sentinelGatewayFilter() {
        return new SentinelGatewayFilter();
    }

    @Bean("sentinel-json-gw-flow-converter")
    public JsonConverter jsonGatewayFlowConverter() {
        return new JsonConverter(objectMapper, GatewayFlowRule.class);
    }

    @Bean("sentinel-json-gw-api-group-converter")
    public JsonConverter jsonGatewayApiGroupConverter() {
        return new ApiDefinitionJsonConverter(objectMapper);
    }

    /**
     *
     * 由于ApiDefinition包含接口ApiPredicateItem，不能直接使用ObjectMapper反序列化
     * 这里提供ApiDefinition反序列化逻辑
     *
     */
    public static class ApiDefinitionJsonConverter extends JsonConverter{

        private static final Logger logger = LoggerFactory.getLogger(SentinelConverter.class);

        private final ObjectMapper objectMapper;

        public ApiDefinitionJsonConverter(ObjectMapper objectMapper) {
            super(objectMapper, ApiDefinition.class);
            this.objectMapper = objectMapper;
        }

        @Override
        public Collection<Object> convert(String source) {
            Collection<Object> ruleCollection = new HashSet<>();

            if (StringUtils.isEmpty(source)) {
                logger.warn("converter can not convert rules because source is empty");
                return ruleCollection;
            }
            try {
                List sourceArray = objectMapper.readValue(source,
                        new TypeReference<List<HashMap>>() {
                        });

                for (Object obj : sourceArray) {
                    String item = null;
                    try {
                        item = objectMapper.writeValueAsString(obj);
                        Optional.ofNullable(convertRule(item))
                                .ifPresent(convertRule -> ruleCollection.add(convertRule));
                    }
                    catch (IOException e) {
                        logger.error("sentinel rule convert error: " + e.getMessage(), e);
                        throw new IllegalArgumentException(
                                "sentinel rule convert error: " + e.getMessage(), e);
                    }
                }
            }
            catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                else {
                    throw new RuntimeException("convert error: " + e.getMessage(), e);
                }
            }
            return ruleCollection;
        }

        private Object convertRule(String ruleStr) throws IOException {
            ApiDefinition apiDefinition = new ApiDefinition();
            Map<String, Object> apiMeta = objectMapper.readValue(ruleStr, Map.class);
            String apiName = (String) apiMeta.get("apiName");

            apiDefinition.setApiName(apiName);
            List<Map> apiPredicateItemsMeta = (List<Map>) apiMeta.get("predicateItems");
            if(apiPredicateItemsMeta != null && !apiPredicateItemsMeta.isEmpty()) {
                Set<ApiPredicateItem> items = new HashSet<>();
                for(Map itemMeta : apiPredicateItemsMeta) {
                    ApiPathPredicateItem apiPathPredicateItem = new ApiPathPredicateItem();
                    itemMeta.forEach((key, item)->{
                        if(key.equals("pattern")) {
                            apiPathPredicateItem.setPattern((String) item);
                        }
                        else if(key.equals("matchStrategy")) {
                            apiPathPredicateItem.setMatchStrategy((Integer) item);
                        }
                    });
                    items.add(apiPathPredicateItem);
                }
                apiDefinition.setPredicateItems(items);
            }

            return apiDefinition;

        }

    }

}
