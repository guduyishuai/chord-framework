package com.chord.framework.boot.autoconfigure.sentinel;

import com.alibaba.cloud.sentinel.SentinelProperties;
import com.alibaba.csp.sentinel.context.Context;
import com.chord.framework.boot.autoconfigure.sentinel.core.SentinelWritableDataSourceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 *
 * Created on 2020/5/15
 *
 * @author: wulinfeng
 */
@ConditionalOnClass(Context.class)
@Configuration
public class ChordSentinelAutoConfiguration {

    private final static Logger logger = LoggerFactory.getLogger(ChordSentinelAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public SentinelWritableDataSourceHandler sentinelWritableDataSourceHandler(
            ObjectProvider<SentinelProperties> sentinelProperties,
            Environment environment,
            DefaultListableBeanFactory beanFactory) {
        return new SentinelWritableDataSourceHandler(beanFactory, sentinelProperties.getIfAvailable(), environment);
    }

}
