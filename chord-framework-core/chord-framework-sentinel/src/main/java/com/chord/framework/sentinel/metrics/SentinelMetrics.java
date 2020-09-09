package com.chord.framework.sentinel.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.boot.actuate.autoconfigure.metrics.export.prometheus.PrometheusProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.*;

/**
 *
 * 获取sentinel的监控信息，转换为micrometer的监控指标
 *
 * Created on 2020/8/5
 *
 * @author: wulinfeng
 */
public class SentinelMetrics implements MeterBinder {

    private static final Logger logger = LoggerFactory.getLogger(SentinelMetrics.class);

    private static final String APPLICATION_NAME_TAG = "application-name";
    private static final String RESOURCE_TAG = "resource";

    public static final String LOCALHOST = "127.0.0.1";

    private static final String PASS_QPS = "sentinel_pass_qps_";
    private static final String BLOCK_QPS = "sentinel_block_qps_";
//    private static final String SUCCESS_QPS = "sentinel_success_qps_";
//    private static final String EXCEPTION_QPS = "sentinel_exception_qps_";
    private static final String RT = "sentinel_rt_";
//    private static final String COUNT = "sentinel_count_";

    private final SentinelMetricsFetcher sentinelMetricsFetcher;

    private ApplicationContext applicationContext;

    private ServerProperties serverProperties;

    private PrometheusProperties prometheusProperties;

    private Map<String, Long> fetchTimeHolder = new HashMap<>(16);

    public SentinelMetrics(SentinelMetricsFetcher sentinelMetricsFetcher,
                           ApplicationContext applicationContext,
                           ServerProperties serverProperties,
                           PrometheusProperties prometheusProperties) {
        this.sentinelMetricsFetcher = sentinelMetricsFetcher;
        this.applicationContext = applicationContext;
        this.serverProperties = serverProperties;
        this.prometheusProperties = prometheusProperties;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        register(registry);
    }

    protected void register(MeterRegistry registry) {

        List<Tag> tags = new ArrayList<>();

        // 获得所有的controller
        Map<String, HandlerMapping> matchingBeans =
                BeanFactoryUtils.beansOfTypeIncludingAncestors(this.applicationContext, HandlerMapping.class, true, false);

        for(HandlerMapping handlerMapping : matchingBeans.values()) {
            if(handlerMapping instanceof RequestMappingHandlerMapping) {
                RequestMappingHandlerMapping requestMappingHandlerMapping = (RequestMappingHandlerMapping) handlerMapping;
                Map<RequestMappingInfo, HandlerMethod> handlerMethodMap = requestMappingHandlerMapping.getHandlerMethods();
                // 注册监控到方法上，这种监控指标可以监控到block，比如1次pass，1次block
                for(HandlerMethod handlerMethod : handlerMethodMap.values()) {
                    String resource = handlerMethod.getBeanType().getSimpleName() + "-" + handlerMethod.getMethod().getName();
                    regist(registry, resource);
                }
                // 注册监控到请求地址上，这种监控不能监控到block，比如1次pass，1次block，将显示为2次pass
                for(RequestMappingInfo requestMappingInfo : handlerMethodMap.keySet()) {
                    Set<String> patterns = requestMappingInfo.getPatternsCondition().getPatterns();
                    for(String resource : patterns) {
                        regist(registry, resource);
                    }
                }
            }
        }

    }

    private void regist(MeterRegistry registry, String resource) {

        List<Tag> tags = Arrays.asList(Tag.of(RESOURCE_TAG, resource));
        String address = LOCALHOST;
        String resourceInfo = address + "_" + resource;
        Gauge.builder(PASS_QPS +  resourceInfo, sentinelMetricsFetcher, sentinelMetricsFetcher->
            new MetricsHandler().handle(sentinelMetricsFetcher, PASS_QPS +  resourceInfo, address)
        ).tags(tags).register(registry);
        Gauge.builder(BLOCK_QPS + resourceInfo, sentinelMetricsFetcher, sentinelMetricsFetcher->
            new MetricsHandler().handle(sentinelMetricsFetcher, BLOCK_QPS +  resourceInfo, address)
        ).tags(tags).register(registry);
//        Gauge.builder(SUCCESS_QPS + resourceInfo, sentinelMetricsFetcher, sentinelMetricsFetcher->
//           new MetricsHandler().handle(sentinelMetricsFetcher, SUCCESS_QPS +  resourceInfo, address)
//        ).tags(tags).register(registry);
//        Gauge.builder(EXCEPTION_QPS + resourceInfo, sentinelMetricsFetcher, sentinelMetricsFetcher->
//            new MetricsHandler().handle(sentinelMetricsFetcher, EXCEPTION_QPS +  resourceInfo, address)
//        ).tags(tags).register(registry);
        Gauge.builder(RT + resourceInfo, sentinelMetricsFetcher, sentinelMetricsFetcher->
            new MetricsHandler().handle(sentinelMetricsFetcher, RT +  resourceInfo, address)
        ).tags(tags).register(registry);
//        FunctionCounter.builder(COUNT + resourceInfo, sentinelMetricsFetcher, sentinelMetricsFetcher->
//            new MetricsHandler().handle(sentinelMetricsFetcher, COUNT +  resourceInfo, address)
//      ).tags(tags).register(registry);

    }

    class MetricsHandler {

        Double handle(SentinelMetricsFetcher sentinelMetricsFetcher, String resource, String ip) {
            List<MetricEntity> metricEntities;
            long now = System.currentTimeMillis();
            long lastFetchTime = fetchTimeHolder.getOrDefault(resource, now);
            fetchTimeHolder.put(RT +  resource, now);
            try {
                metricEntities = sentinelMetricsFetcher.fetch(ip, lastFetchTime, now);
            } catch (Exception e) {
                return 0D;
            }
            for(MetricEntity metricEntity : metricEntities) {
                if(metricEntity.getResource().equals(resource)) {
                    return metricEntity.getRt();
                }
            }
            return 0D;
        }

    }

}
