package com.chord.framework.kafka.consumer;

import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;

/**
 *
 * 预留出来的模板类，表示通用的设置，目前全部使用默认设置
 *
 * Created on 2020/7/17
 *
 * @author: wulinfeng
 */
public class NormalKafkaListenerContainerFactory<K, V> extends ConcurrentKafkaListenerContainerFactory<K, V> {



}
