package com.chord.framework.boot.autoconfigure.kafka;

import com.chord.framework.kafka.consumer.EosKafkaConsumerFactory;
import com.chord.framework.kafka.consumer.EosKafkaListenerContainerFactory;
import com.chord.framework.kafka.producer.EosKafkaProducerFactory;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaProducerFactoryCustomizer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.AfterRollbackProcessor;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DefaultAfterRollbackProcessor;
import org.springframework.kafka.transaction.ChainedKafkaTransactionManager;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.util.backoff.ExponentialBackOff;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Created on 2020/7/17
 *
 * @author: wulinfeng
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(KafkaTemplate.class)
public class EosKafkaAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(EosKafkaAutoConfiguration.class);

    private final KafkaProperties properties;

    public EosKafkaAutoConfiguration(KafkaProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean(ProducerFactory.class)
    public ProducerFactory<?, ?> kafkaProducerFactory(
            ObjectProvider<DefaultKafkaProducerFactoryCustomizer> customizers) {
        DefaultKafkaProducerFactory<?, ?> factory = new EosKafkaProducerFactory<>(
                this.properties.buildProducerProperties(), properties, new StringSerializer(), new StringSerializer());
        customizers.orderedStream().forEach((customizer) -> customizer.customize(factory));
        return factory;
    }

    @Bean
    @ConditionalOnBean(DataSourceTransactionManager.class)
    @ConditionalOnMissingBean
    public ChainedKafkaTransactionManager<Object, Object> chainedTm(
            KafkaTransactionManager<Object, Object> tm,
            DataSourceTransactionManager dstm) {
        return new ChainedKafkaTransactionManager<>(tm, dstm);
    }

    @Bean
    @ConditionalOnProperty(name = "spring.kafka.producer.transaction-id-prefix")
    @ConditionalOnMissingBean
    public KafkaTransactionManager<Object, Object> tm(ProducerFactory<?, ?> kafkaProducerFactory) {
        return new KafkaTransactionManager(kafkaProducerFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public EosKafkaConsumerFactory kafkaConsumerFactory() {
        return new EosKafkaConsumerFactory(this.properties.buildConsumerProperties(), new StringDeserializer(), new StringDeserializer());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> kafkaConsumerFactory,
            ObjectProvider<ChainedKafkaTransactionManager> chainedTM,
            ObjectProvider<KafkaTransactionManager> tm,
            ObjectProvider<AfterRollbackProcessor> afterRollbackProcessor) {

        EosKafkaListenerContainerFactory<Object, Object> factory;

        if(chainedTM.getIfAvailable() != null) {
            factory = new EosKafkaListenerContainerFactory<>(chainedTM.getIfAvailable());
        } else {
            factory = new EosKafkaListenerContainerFactory<>(tm.getIfAvailable());
        }

        if(afterRollbackProcessor.getIfAvailable() != null) {
            factory.setAfterRollbackProcessor(afterRollbackProcessor.getIfAvailable());
        }

        configurer.configure(factory, kafkaConsumerFactory);
        return factory;

    }

    @Bean
    @ConditionalOnMissingBean
    public AfterRollbackProcessor<Object, Object> afterRollbackProcessor(ObjectProvider<ConsumerRecordRecoverer> consumerRecordRecoverer) {
        ExponentialBackOff exponentialBackOff = new ExponentialBackOff(500L, 1.5);
        exponentialBackOff.setMaxElapsedTime(60000);
        return new DefaultAfterRollbackProcessor(consumerRecordRecoverer.getIfAvailable(
                ()-> (record, ex) -> logger.error("Failed to process " + record, ex)
        ), exponentialBackOff);
    }

}
