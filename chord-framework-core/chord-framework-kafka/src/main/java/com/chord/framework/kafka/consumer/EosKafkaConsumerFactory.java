package com.chord.framework.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.IsolationLevel;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.util.ReflectionUtils;

import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Created on 2020/7/17
 *
 * @author: wulinfeng
 */
public class EosKafkaConsumerFactory<K, V> extends NormalKafkaConsumerFactory<K, V> {

    public EosKafkaConsumerFactory(Map<String, Object> configs) {
        super(configs);
        initEos();
    }

    public EosKafkaConsumerFactory(Map<String, Object> configs, Deserializer<K> keyDeserializer, Deserializer<V> valueDeserializer) {
        super(configs, keyDeserializer, valueDeserializer);
        initEos();
    }

    public EosKafkaConsumerFactory(Map<String, Object> configs, Supplier<Deserializer<K>> keyDeserializerSupplier, Supplier<Deserializer<V>> valueDeserializerSupplier) {
        super(configs, keyDeserializerSupplier, valueDeserializerSupplier);
        initEos();
    }

    private void initEos() {

        ReflectionUtils.doWithLocalFields(DefaultKafkaConsumerFactory.class, field -> {
            if (field.getName().equals("configs")) {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                Map<String, Object> configs = (Map<String, Object>) field.get(this);

                configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
                configs.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
                configs.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, IsolationLevel.READ_COMMITTED.name().toLowerCase(Locale.ROOT));

            }

        });

    }

}
