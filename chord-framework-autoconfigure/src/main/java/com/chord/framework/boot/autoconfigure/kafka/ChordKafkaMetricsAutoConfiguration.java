package com.chord.framework.boot.autoconfigure.kafka;

import com.chord.framework.kafka.metrics.KafkaConsumerMetrics;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.kafka.KafkaClientMetrics;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ProducerFactory;

/**
 *
 * 用来注册kafaka消费端的监控数据的{@link MeterBinder}，{@link MeterBinder}是micrometer中的类
 * micrometer类似于日志的slf4j，通过{@link MeterBinder}可以适配任何监控
 *
 * Created on 2020/7/24
 *
 * @author: wulinfeng
 */
@AutoConfigureAfter(MetricsAutoConfiguration.class)
@ConditionalOnClass({ KafkaClientMetrics.class, ProducerFactory.class })
@Configuration(proxyBeanMethods = false)
public class ChordKafkaMetricsAutoConfiguration {

    @Bean
    public MeterBinder chordKafkaConsumerMetrics() {
        return new KafkaConsumerMetrics();
    }

}
