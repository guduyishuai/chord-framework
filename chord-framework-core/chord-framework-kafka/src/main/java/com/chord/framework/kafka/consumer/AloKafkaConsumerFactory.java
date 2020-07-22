package com.chord.framework.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.util.ReflectionUtils;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Created on 2020/7/21
 *
 * @author: wulinfeng
 */
public class AloKafkaConsumerFactory <K, V> extends NormalKafkaConsumerFactory<K, V> {

    public AloKafkaConsumerFactory(Map<String, Object> configs) {
        super(configs);
    }

    public AloKafkaConsumerFactory(Map<String, Object> configs, Deserializer<K> keyDeserializer, Deserializer<V> valueDeserializer) {
        super(configs, keyDeserializer, valueDeserializer);
    }

    public AloKafkaConsumerFactory(Map<String, Object> configs, Supplier<Deserializer<K>> keyDeserializerSupplier, Supplier<Deserializer<V>> valueDeserializerSupplier) {
        super(configs, keyDeserializerSupplier, valueDeserializerSupplier);
    }

    private void initAlo() {

        ReflectionUtils.doWithLocalFields(DefaultKafkaConsumerFactory.class, field -> {
            if (field.getName().equals("configs")) {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                Map<String, Object> configs = (Map<String, Object>) field.get(this);

                // 不是用自动提交
                configs.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

                // 防止消息丢失
                configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

            }
        });

    }

}
