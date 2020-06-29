package com.chord.framework.boot.autoconfigure.mutex.lock;

import com.chord.framework.boot.autoconfigure.common.ValidationUtils;
import com.chord.framework.boot.autoconfigure.mutex.lock.conditional.ConditionalOnZookeeperLock;
import com.chord.framework.boot.autoconfigure.mutex.lock.properties.MutexLockProperties;
import com.chord.framework.boot.autoconfigure.mutex.lock.properties.ZookeeperDataSource;
import com.chord.framework.boot.autoconfigure.mutex.lock.wrapper.ZookeeeperLockFactoryWrapper;
import com.chord.framework.mutex.lock.zookeeper.ZookeeperConfiguration;
import com.chord.framework.mutex.lock.zookeeper.ZookeeperLockFactory;
import org.apache.curator.RetryPolicy;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.io.UnsupportedEncodingException;

/**
 * Created on 2020/6/28
 *
 * @author: wulinfeng
 */
@ConditionalOnZookeeperLock
@Configuration
public class ZookeeperLockAutoConfiguration {

    @Bean
    public ZookeeperConfiguration zookeeperConfiguration(MutexLockProperties mutexLockProperties, RetryPolicy retryPolicy) {
        if(mutexLockProperties.getZookeeper() == null) {
            throw new IllegalArgumentException("not found the config for zookeeper");
        }

        ZookeeperDataSource zookeeperDataSource = mutexLockProperties.getZookeeper();
        new ValidationUtils<ZookeeperDataSource>().validate(zookeeperDataSource);

        ZookeeperConfiguration zookeeperConfiguration = new ZookeeperConfiguration();
        zookeeperConfiguration.setConnectString(zookeeperDataSource.getConnectString());
        zookeeperConfiguration.setLockPath(zookeeperDataSource.getLockPath());
        zookeeperConfiguration.setConnectionTimeoutMs(zookeeperDataSource.getSessionTimeoutMs());
        zookeeperConfiguration.setSessionTimeoutMs(zookeeperDataSource.getSessionTimeoutMs());
        zookeeperConfiguration.setRetryPolicy(retryPolicy);

        return zookeeperConfiguration;
    }

    @Bean
    @ConditionalOnMissingBean
    public RetryPolicy retryPolicy() {
        return new ExponentialBackoffRetry(1000, 3);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @ConditionalOnMissingBean
    public ZookeeperLockFactory zookeeperLockFactory(ZookeeperConfiguration zookeeperConfiguration) throws UnsupportedEncodingException {
        return new ZookeeperLockFactory(zookeeperConfiguration);
    }

    @Bean
    public ZookeeeperLockFactoryWrapper zookeeeperLockFactoryWrapper() {
        return new ZookeeeperLockFactoryWrapper();
    }

}
