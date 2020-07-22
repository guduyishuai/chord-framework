package com.chord.framework.kafka.producer;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.util.ReflectionUtils;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Created on 2020/7/21
 *
 * @author: wulinfeng
 */
public class AloKafkaProducerFactory<K, V> extends NormalKafkaProducerFactory<K, V> {

    public AloKafkaProducerFactory(Map<String, Object> configs) {
        super(configs);
        initAto();
    }

    public AloKafkaProducerFactory(Map<String, Object> configs, Serializer<K> keySerializer, Serializer<V> valueSerializer) {
        super(configs, keySerializer, valueSerializer);
        initAto();
    }

    public AloKafkaProducerFactory(Map<String, Object> configs, Supplier<Serializer<K>> keySerializerSupplier, Supplier<Serializer<V>> valueSerializerSupplier) {
        super(configs, keySerializerSupplier, valueSerializerSupplier);
        initAto();
    }

    private void initAto() {

        ReflectionUtils.doWithLocalFields(DefaultKafkaProducerFactory.class, field -> {
            if (field.getName().equals("configs")) {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                Map<String, Object> configs = (Map<String, Object>) field.get(this);

                configs.put(
                        ProducerConfig.RETRIES_CONFIG,
                        Integer.MAX_VALUE
                );

                // 0表示仅仅发送，不需要等待broker的返回，retries配置将不起作用
                // 1表示只需要leader写了日志后则返回成功，不需要等follower同步
                //  如果leader写了日志后马上宕机，新选举的leader将没有刚发送的消息，造成消息丢失
                // all表示需要leader写日志，并且ISR中的所有follower同步了日志，才返回成功
                configs.put(
                        ProducerConfig.ACKS_CONFIG,
                        "all"
                );

            }
        });

    }

}
