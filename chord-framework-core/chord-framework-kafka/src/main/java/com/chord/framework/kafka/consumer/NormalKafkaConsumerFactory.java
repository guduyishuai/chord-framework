package com.chord.framework.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.StickyAssignor;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.util.ReflectionUtils;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Created on 2020/7/17
 *
 * @author: wulinfeng
 */
public class NormalKafkaConsumerFactory<K, V> extends DefaultKafkaConsumerFactory<K, V> {

    public NormalKafkaConsumerFactory(Map<String, Object> configs) {
        super(configs);
        initNormal();
    }

    public NormalKafkaConsumerFactory(Map<String, Object> configs, Deserializer<K> keyDeserializer, Deserializer<V> valueDeserializer) {
        super(configs, keyDeserializer, valueDeserializer);
        initNormal();
    }

    public NormalKafkaConsumerFactory(Map<String, Object> configs, Supplier<Deserializer<K>> keyDeserializerSupplier, Supplier<Deserializer<V>> valueDeserializerSupplier) {
        super(configs, keyDeserializerSupplier, valueDeserializerSupplier);
        initNormal();
    }

    private void initNormal() {

        ReflectionUtils.doWithLocalFields(DefaultKafkaConsumerFactory.class, field -> {
            if (field.getName().equals("configs")) {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                Map<String, Object> configs = (Map<String, Object>) field.get(this);

                // 每次poll获取最大的消息数，这里比默认的500增加了一些
                configs.putIfAbsent(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1000);
                // 两次poll的间隔时间，需要保证业务处理完，不然两次poll超过这个时间，就会重平衡，使用默认的3分钟
                configs.putIfAbsent(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000);
                // 心跳超时时间，越短越好，及早剔除失效的消费者，进行重平衡，使用默认的10秒
                configs.putIfAbsent(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 10000);
                // 类似于SESSION_TIMEOUT_MS_CONFIG，不超过类似于SESSION_TIMEOUT_MS_CONFIG的1/3，使用默认的3秒
                configs.putIfAbsent(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 3000);
                // 使用粘性的分配策略，考虑上一次分配结果，减少变动量，减少开销
                configs.putIfAbsent(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, StickyAssignor.class.getName());
                // 不是用自动提交
                configs.putIfAbsent(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
                // 只有broker的消息达到了该值才会发送给消费者，消费者从服务器获取的最小字节，当主题不活跃时，可以加大该值，降低broker和消费者的工作负载，默认值1，表示一有消息就发送给消费者
                // 如果数据量不大，消费者CPU很高，可以加大该值
                // 消费者比较多，加大该值，可以降低broker的工作负载
                // 调整为1MB
                configs.putIfAbsent(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1024);
                // 单次获取的最大消息数，使用默认的值
                configs.putIfAbsent(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, ConsumerConfig.DEFAULT_FETCH_MAX_BYTES);
                // 不满足FETCH_MIN_BYTES_CONFIG，但是该数据在broker停留的时间达到该值，也会发送消息给消费者,使用默认的500ms
                configs.putIfAbsent(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);
                // 防止消息丢失
                configs.putIfAbsent(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
                // 从每个分区获取消息的最大字节数，使用默认值
                configs.putIfAbsent(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, ConsumerConfig.DEFAULT_MAX_PARTITION_FETCH_BYTES);
                // tcp缓存，使用默认值
                configs.putIfAbsent(ConsumerConfig.SEND_BUFFER_CONFIG, 128 * 1024);
                // tcp缓存，使用默认值
                configs.putIfAbsent(ConsumerConfig.RECEIVE_BUFFER_CONFIG, 64 * 1024);
                // 重试时间，默认100ms
                configs.putIfAbsent(ConsumerConfig.RETRY_BACKOFF_MS_CONFIG, 100L);

            }

        });

    }

}
