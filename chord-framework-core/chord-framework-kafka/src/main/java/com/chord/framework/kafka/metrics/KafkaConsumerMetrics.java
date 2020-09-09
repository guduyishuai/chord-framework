package com.chord.framework.kafka.metrics;

import io.micrometer.core.instrument.MeterRegistry;

import java.util.Arrays;
import java.util.List;

/**
 *
 * kafka的consumer监控数据
 * @see KafkaMetrics
 *
 * Created on 2020/7/24
 *
 * @author: wulinfeng
 */
public class KafkaConsumerMetrics extends KafkaMetrics {

    @Override
    public void bindTo(MeterRegistry registry) {
        registerConsumerMetrics(registry);
        registerConsumerCoordinatorMetrics(registry);
        registerConsumerFetchManagerMetrics(registry);
    }

    private void registerConsumerMetrics(MeterRegistry registry) {

        registerGauges(
                registry,
                KafkaConsumerMetricsMeta.CONSUMER_METRICS,
                KafkaConsumerMetricsMeta.CONSUMER_GROUP,
                KafkaConsumerMetricsMeta.CONSUMER_PREFFIX,
                KafkaConsumerMetricsMeta.CONSUMER_GAUGE);


        registerFunctionCounters(
                registry,
                KafkaConsumerMetricsMeta.CONSUMER_METRICS,
                KafkaConsumerMetricsMeta.CONSUMER_GROUP,
                KafkaConsumerMetricsMeta.CONSUMER_PREFFIX,
                KafkaConsumerMetricsMeta.COUNSMER_COUNTER);

    }

    private void registerConsumerCoordinatorMetrics(MeterRegistry registry) {

        registerGauges(
                registry,
                KafkaConsumerMetricsMeta.CONSUMER_METRICS,
                KafkaConsumerMetricsMeta.CONSUMER_COORDINATOR_GROUP,
                KafkaConsumerMetricsMeta.CONSUMER_COORDINATOR_PREFFIX,
                KafkaConsumerMetricsMeta.CONSUMER_COORDINATOR_GAUGE);

        registerFunctionCounters(
                registry,
                KafkaConsumerMetricsMeta.CONSUMER_METRICS,
                KafkaConsumerMetricsMeta.CONSUMER_COORDINATOR_GROUP,
                KafkaConsumerMetricsMeta.CONSUMER_COORDINATOR_PREFFIX,
                KafkaConsumerMetricsMeta.CONSUMER_COORDINATOR_COUNTER);

    }

    private void registerConsumerFetchManagerMetrics(MeterRegistry registry) {

        registerGauges(
                registry,
                KafkaConsumerMetricsMeta.CONSUMER_METRICS,
                KafkaConsumerMetricsMeta.CONSUMER_FETCH_MANAGER_GROUP,
                KafkaConsumerMetricsMeta.CONSUMER_FETCH_MANAGER_PREFFIX,
                KafkaConsumerMetricsMeta.CONSUMER_FETCH_GAUGE);

        registerFunctionCounters(
                registry,
                KafkaConsumerMetricsMeta.CONSUMER_METRICS,
                KafkaConsumerMetricsMeta.CONSUMER_FETCH_MANAGER_GROUP,
                KafkaConsumerMetricsMeta.CONSUMER_FETCH_MANAGER_PREFFIX,
                KafkaConsumerMetricsMeta.CONSUMER_FETCH_COUNTER);

    }

    /**
     *
     * kafka自带jmx监控的元数据信息
     *
     */
    static class KafkaConsumerMetricsMeta {

        public static final String CONSUMER_METRICS = "kafka.consumer";

        public static final String CONSUMER_GROUP = "consumer-metrics";
        public static final String CONSUMER_PREFFIX = CONSUMER_METRICS;

        public static final String CONSUMER_COORDINATOR_GROUP = "consumer-coordinator-metrics";
        public static final String CONSUMER_COORDINATOR_PREFFIX = "kafka.consumer.coordinator";

        public static final String CONSUMER_FETCH_MANAGER_GROUP = "consumer-fetch-manager-metrics";
        public static final String CONSUMER_FETCH_MANAGER_PREFFIX = "kafka.consumer.fetch.manager";

        public static List<String> CONSUMER_GAUGE;
        public static List<String> COUNSMER_COUNTER;
        public static List<String> CONSUMER_COORDINATOR_GAUGE;
        public static List<String> CONSUMER_COORDINATOR_COUNTER;
        public static List<String> CONSUMER_FETCH_GAUGE;
        public static List<String> CONSUMER_FETCH_COUNTER;

        static {

            CONSUMER_GAUGE = Arrays.asList("response-rate", "select-rate", "network-io-rate", "io-ratio", "io-wait-ratio",
                    "outgoing-byte-rate", "successful-authentication-rate", "failed-authentication-rate",
                    "incoming-byte-rate", "connection-close-rate", "request-size-max", "request-size-avg", "iotime-total",
                    "connection-creation-rate", "io-wait-time-ns-avg", "io-time-ns-avg", "request-rate");

            COUNSMER_COUNTER = Arrays.asList("connection-creation-total", "connection-close-total", "request-total",
                    "network-io-total", "incoming-byte-total", "response-total", "iotime-total",
                    "successful-authentication-total", "connection-count", "io-waittime-total",
                    "failed-authentication-total", "select-total", "outgoing-byte-total");

            CONSUMER_COORDINATOR_GAUGE = Arrays.asList("join-time-max", "commit-latency-avg", "sync-time-avg", "join-rate",
                    "assigned-partitions", "sync-rate", "commit-rate", "last-heartbeat-seconds-ago", "heartbeat-rate",
                    "commit-latency-max", "join-time-avg", "sync-time-max", "heartbeat-response-time-max");

            CONSUMER_COORDINATOR_COUNTER = Arrays.asList("sync-total", "commit-total", "heartbeat-total", "join-total");

            CONSUMER_FETCH_GAUGE = Arrays.asList("bytes-consumed-rate", "fetch-latency-max", "fetch-rate", "fetch-throttle-time-max",
                    "fetch-size-max", "fetch-latency-avg", "records-lag-max", "records-consumed-rate",
                    "fetch-throttle-time-avg", "fetch-size-avg", "records-per-request-avg");

            CONSUMER_FETCH_COUNTER = Arrays.asList("fetch-total", "records-consumed-total", "bytes-consumed-total");

        }

    }

}
