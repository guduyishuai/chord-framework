package com.chord.framework.kafka.metrics;

import com.chord.framework.metrics.BaseMetricsAdapter;
import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.binder.MeterBinder;

import java.util.List;

/**
 * Created on 2020/7/24
 *
 * @author: wulinfeng
 */
public abstract class KafkaMetrics extends BaseMetricsAdapter implements MeterBinder {

    private static final String OBJECT_NAME_KEY = "type";

    private static final String CLIENT_ID_PROPERTY = "client-id";

    private static final String CLIENT_ID_TAG = "client-id";

    public KafkaMetrics() {
        super();
    }

    public KafkaMetrics(Iterable<Tag> tags) {
        super(tags);
    }

    protected void registerGauges(MeterRegistry registry, String objName, String group, String preffix, List<String> metrics) {
        registerMetrics(objName, OBJECT_NAME_KEY, group, (name, allTags) -> {
            String clientId = name.getKeyProperty(CLIENT_ID_PROPERTY);

            metrics.forEach(metric -> {
                String metricName = preffix + "." + metric;

                Gauge.builder(metricName, mBeanServer, s -> safeDouble(() -> s.getAttribute(name, metric)))
                        .tags(Tags.of(CLIENT_ID_TAG, clientId)).register(registry);

            });
        });
    }

    protected void registerFunctionCounters(MeterRegistry registry, String objName, String group, String preffix,
                                            List<String> metrics) {
        registerMetrics(objName, OBJECT_NAME_KEY, group, (name, allTags) -> {
            String clientId = name.getKeyProperty(CLIENT_ID_PROPERTY);

            metrics.forEach(metric -> {
                String metricName = preffix + "." + metric;

                FunctionCounter.builder(metricName, mBeanServer, s -> safeDouble(() -> s.getAttribute(name, metric)))
                        .tags(Tags.of(CLIENT_ID_TAG, clientId)).register(registry);
            });
        });
    }

}
