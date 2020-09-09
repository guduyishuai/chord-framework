package com.chord.framework.boot.autoconfigure.sentinel;

import com.alibaba.csp.sentinel.context.Context;
import com.chord.framework.sentinel.metrics.SentinelMetrics;
import com.chord.framework.sentinel.metrics.SentinelMetricsFetcher;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.prometheus.PrometheusMetricsExportAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.prometheus.PrometheusProperties;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.UnknownHostException;

/**
 *
 * 用来注册sentinel的监控数据的{@link MeterBinder}，{@link MeterBinder}是micrometer中的类
 * micrometer类似于日志的slf4j，通过{@link MeterBinder}可以适配任何监控
 *
 * Created on 2020/8/6
 *
 * @author: wulinfeng
 */
@AutoConfigureAfter({MetricsAutoConfiguration.class, PrometheusMetricsExportAutoConfiguration.class})
@ConditionalOnClass({Context.class, PrometheusMeterRegistry.class})
@ConditionalOnProperty(prefix = "management.metrics.export.prometheus", name = "enabled", havingValue = "true",
        matchIfMissing = true)
@Configuration(proxyBeanMethods = false)
public class ChordSentinelMetricAutoConfiguration {

    @Bean
    public SentinelMetricsFetcher sentinelMetricsFetcher() throws UnknownHostException {
        return new SentinelMetricsFetcher();
    }

    @Bean
    public MeterBinder chordSentinelMetrics(
            SentinelMetricsFetcher sentinelMetricsFetcher,
            ApplicationContext applicationContext,
            ServerProperties serverProperties,
            PrometheusProperties prometheusProperties) {
        return new SentinelMetrics(sentinelMetricsFetcher, applicationContext, serverProperties, prometheusProperties);
    }

}
