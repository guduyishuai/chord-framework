package com.chord.framework.kafka.producer;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.util.ReflectionUtils;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Created on 2020/7/16
 *
 * @author: wulinfeng
 */
public class EosKafkaProducerFactory<K, V> extends NormalKafkaProducerFactory<K, V> {

    public EosKafkaProducerFactory(Map<String, Object> configs, KafkaProperties kafkaProperties) {
        super(configs);
        initEos(kafkaProperties);
    }

    public EosKafkaProducerFactory(Map<String, Object> configs, KafkaProperties kafkaProperties, Serializer<K> keySerializer, Serializer<V> valueSerializer) {
        super(configs, keySerializer, valueSerializer);
        initEos(kafkaProperties);
    }

    public EosKafkaProducerFactory(Map<String, Object> configs, KafkaProperties kafkaProperties, Supplier<Serializer<K>> keySerializerSupplier, Supplier<Serializer<V>> valueSerializerSupplier) {
        super(configs, keySerializerSupplier, valueSerializerSupplier);
        initEos(kafkaProperties);
    }

    private void initEos(KafkaProperties kafkaProperties) {

        // EosMode为Beta，取消每个分区一个Producer
        // 事务信息中会带有相关的元数据隔离僵尸实例
        super.setProducerPerConsumerPartition(false);

        // 事务前缀
        super.setTransactionIdPrefix(kafkaProperties.getProducer().getTransactionIdPrefix());

        ReflectionUtils.doWithLocalFields(DefaultKafkaProducerFactory.class, field -> {
            if(field.getName().equals("configs")) {
                if(!field.isAccessible()) {
                    field.setAccessible(true);
                }

                Map<String, Object> configs = (Map<String, Object>) field.get(this);

                // 启用幂等性，设置了事务id，自动会启用幂等性，这里只是体现一下设置幂等性为true
                configs.put(
                        ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,
                        true
                );
            }
        });

    }

}
