package com.chord.framework.kafka.producer;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.record.CompressionType;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.util.ReflectionUtils;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Created on 2020/7/17
 *
 * @author: wulinfeng
 */
public class NormalKafkaProducerFactory<K, V> extends DefaultKafkaProducerFactory<K, V> {

    public NormalKafkaProducerFactory(Map<String, Object> configs) {
        super(configs);
        initNomal();
    }

    public NormalKafkaProducerFactory(Map<String, Object> configs, Serializer<K> keySerializer, Serializer<V> valueSerializer) {
        super(configs, keySerializer, valueSerializer);
        initNomal();
    }

    public NormalKafkaProducerFactory(Map<String, Object> configs, Supplier<Serializer<K>> keySerializerSupplier, Supplier<Serializer<V>> valueSerializerSupplier) {
        super(configs, keySerializerSupplier, valueSerializerSupplier);
        initNomal();
    }

    private void initNomal() {

        ReflectionUtils.doWithLocalFields(DefaultKafkaProducerFactory.class, field -> {
            if (field.getName().equals("configs")) {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                Map<String, Object> configs = (Map<String, Object>) field.get(this);

                // 默认调整到32KB，另外bufferMemory需要结合batchSize和并发调整
                configs.put(
                        ProducerConfig.BATCH_SIZE_CONFIG,
                        32);

                // 默认batch收集时间为30ms
                configs.put(
                        ProducerConfig.LINGER_MS_CONFIG,
                        30
                );

                // 默认调整为LZ4压缩方案
                configs.put(
                        ProducerConfig.COMPRESSION_TYPE_CONFIG,
                        CompressionType.LZ4.name
                );

                // 发送消息超时时间，超过这个时间会重试
                // 必须要大于replica.lag.time.max.ms设置，该设置是broker的设置，该设置表示follower副本落后leader副本后超过该时间还没有赶上的话，就剔除ISR名单
                // 这样做可以在重试减少重复的消息数
                // 使用默认值
                configs.put(
                        ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG,
                        30 * 1000
                );

                // 调用send后，收到成功或者失败的超时时间。这个时间包括了发送消息，等待ack，重试的时间。该配置应该大于request.timeout.ms + linger.ms
                // 使用默认值
                configs.put(
                        ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG,
                        120 * 1000
                );

                // tcp缓存，使用默认值
                configs.put(
                        ProducerConfig.SEND_BUFFER_CONFIG,
                        128 * 1024
                );

                // tcp缓存，使用默认值
                configs.put(
                        ProducerConfig.RECEIVE_BUFFER_CONFIG,
                        32 * 1024
                );

                // buffer的大小，需要结合压测进行调整，使用默认32MB
                configs.put(
                        ProducerConfig.BUFFER_MEMORY_CONFIG,
                        32 * 1024 * 1024L
                );

                // buffer满了，发送的阻塞时间，超过该时间报错，使用默认值6秒
                configs.put(
                        ProducerConfig.MAX_BLOCK_MS_CONFIG,
                        60 * 1000
                );

                // 生产者收到响应前能够发送的消息数，如果要保证顺序消费，包括重试的情况，该值需要设置为1
                // 使用默认值5
                configs.put(
                        ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION,
                        5
                );

                // 重试次数，0.11.1.0以前需要设置MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION为1才能保证顺序
                // 如果0.11.1.0及以后，使用了EOS语义，重试不会造成消息顺序问题
                // 使用默认值
                configs.put(
                        ProducerConfig.RETRIES_CONFIG,
                        Integer.MAX_VALUE
                );

                // 幂等性，如果使用EOS，需要设置为true
                // MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION小于等于5
                // RETRIES_CONFIG大于0
                // ACKS_CONFIG设置为ALL
                configs.put(
                        ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,
                        false
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
