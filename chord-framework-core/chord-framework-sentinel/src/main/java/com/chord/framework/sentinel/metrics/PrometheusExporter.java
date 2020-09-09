package com.chord.framework.sentinel.metrics;

import io.prometheus.client.Collector;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 *
 * 此类没有实现exporter，暂时保留在这里而已
 * 不使用exporter，出于两方面考虑
 * 1、兼容spring-boot的PushGateway方案
 * 2、不方便一次性批量获取sentinel的监控信息
 *
 * Created on 2020/8/4
 *
 * @author: wulinfeng
 */
public class PrometheusExporter implements EnvironmentAware {

    private static final Logger logger = LoggerFactory.getLogger(PrometheusExporter.class);

    private final static long START_TIME = 2882880000000L;

    private final SentinelMetricsFetcher sentinelMetricsFetcher;

    private Environment environment;

    public PrometheusExporter (SentinelMetricsFetcher sentinelMetricsFetcher) {
        this.sentinelMetricsFetcher = sentinelMetricsFetcher;
    }

//    @Override
//    public List<MetricFamilySamples> collect() {
//        List<MetricFamilySamples> familySamples = new ArrayList<>();
//        List<MetricEntity> metricEntities = sentinelMetricsFetcher.fetch(Integer.parseInt(environment.getProperty("server.port")), START_TIME, System.currentTimeMillis());
//        List<MetricFamilySamples.Sample> samples = new ArrayList<>();
//        metricEntities.stream().forEach((metricEntity -> samples.add(convert(metricEntity))));
//        if (!samples.isEmpty()) {
//            familySamples.add(new MetricFamilySamples("sentinel", Type.UNTYPED, "sentinel", samples));
//        }
//        return familySamples;
//    }
//
//    private MetricFamilySamples.Sample convert(MetricEntity metricEntities) {
//        String prometheusName = "sentinel_";
//        List<String> labelNames = new ArrayList<>();
//        List<String> labelValues = new ArrayList<>();
////        for (Map.Entry<String, String> tag : measurement.getId().getTags()) {
////            labelNames.add(tag.getKey());
////            labelValues.add(tag.getValue());
////        }
//        return null;
//    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    static class PassQpsExporter extends Collector {

        @Override
        public List<MetricFamilySamples> collect() {
            return null;
        }

    }

    static class BlockQpsExporter extends Collector {

        @Override
        public List<MetricFamilySamples> collect() {
            return null;
        }

    }

    static class RtAndSuccessQpsExporter extends Collector {

        @Override
        public List<MetricFamilySamples> collect() {
            return null;
        }

    }

    static class ExceptionQpsExporter extends Collector {

        @Override
        public List<MetricFamilySamples> collect() {
            return null;
        }

    }

    static class CounterExporter extends Collector {

        @Override
        public List<MetricFamilySamples> collect() {
            return null;
        }

    }

}
