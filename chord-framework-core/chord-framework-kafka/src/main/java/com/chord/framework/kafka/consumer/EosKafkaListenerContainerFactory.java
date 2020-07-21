package com.chord.framework.kafka.consumer;

import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Created on 2020/7/17
 *
 * @author: wulinfeng
 */
public class EosKafkaListenerContainerFactory<K, V> extends ConcurrentKafkaListenerContainerFactory<K, V> {

    public EosKafkaListenerContainerFactory(PlatformTransactionManager transactionManager) {

        getContainerProperties().setTransactionManager(transactionManager);
        getContainerProperties().setEosMode(ContainerProperties.EOSMode.BETA);
        getContainerProperties().setSubBatchPerPartition(false);

    }

}
