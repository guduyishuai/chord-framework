package com.chord.framework.boot.autoconfigure.kafka;

import com.chord.framework.kafka.consumer.AloKafkaConsumerFactory;
import com.chord.framework.kafka.consumer.NormalKafkaListenerContainerFactory;
import com.chord.framework.kafka.producer.AloKafkaProducerFactory;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaProducerFactoryCustomizer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.ErrorHandler;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

/**
 * Created on 2020/7/21
 *
 * @author: wulinfeng
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(KafkaTemplate.class)
public class AloKafkaAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(AmoKafkaAutoConfiguration.class);

    private final KafkaProperties properties;

    public AloKafkaAutoConfiguration(KafkaProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean(ProducerFactory.class)
    public ProducerFactory<?, ?> kafkaProducerFactory(
            ObjectProvider<DefaultKafkaProducerFactoryCustomizer> customizers) {
        DefaultKafkaProducerFactory<?, ?> factory = new AloKafkaProducerFactory<>(
                this.properties.buildProducerProperties(), new StringSerializer(), new StringSerializer());
        customizers.orderedStream().forEach((customizer) -> customizer.customize(factory));
        return factory;
    }

    @Bean
    @ConditionalOnMissingBean
    public AloKafkaConsumerFactory kafkaConsumerFactory() {
        return new AloKafkaConsumerFactory(this.properties.buildConsumerProperties(), new StringDeserializer(), new StringDeserializer());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> kafkaConsumerFactory,
            ObjectProvider<ErrorHandler> errorHandler) {

        NormalKafkaListenerContainerFactory<Object, Object> factory = new NormalKafkaListenerContainerFactory<>();
        configurer.configure(factory, kafkaConsumerFactory);
        if(errorHandler.getIfAvailable() != null) {
            factory.setErrorHandler(errorHandler.getIfAvailable());
        }
        return factory;

    }

    @Bean
    @ConditionalOnMissingBean
    public ErrorHandler errorHandler(ObjectProvider<ConsumerRecordRecoverer> consumerRecordRecoverer) {
        ExponentialBackOff exponentialBackOff = new ExponentialBackOff(500L, 1.5);
        exponentialBackOff.setMaxElapsedTime(60000);
        return new SeekToCurrentErrorHandler(consumerRecordRecoverer.getIfAvailable(
                ()-> (record, ex) -> logger.error("Failed to process " + record, ex)
        ), exponentialBackOff);
    }

}
