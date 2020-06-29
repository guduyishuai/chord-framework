package com.chord.framework.boot.autoconfigure.mutex.lock.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Created on 2020/6/28
 *
 * @author: wulinfeng
 */
@ConfigurationProperties(prefix = MutexLockProperties.PREFIX)
public class MutexLockProperties {

    public static final String PREFIX = "chord.mutex.lock";

    @NestedConfigurationProperty
    private ZookeeperDataSource zookeeper;

    @NestedConfigurationProperty
    private RedissionDataSource redission;

    @NestedConfigurationProperty
    private RedisDataSource redis;

    public ZookeeperDataSource getZookeeper() {
        return zookeeper;
    }

    public void setZookeeper(ZookeeperDataSource zookeeper) {
        this.zookeeper = zookeeper;
    }

    public RedissionDataSource getRedission() {
        return redission;
    }

    public void setRedission(RedissionDataSource redission) {
        this.redission = redission;
    }

    public RedisDataSource getRedis() {
        return redis;
    }

    public void setRedis(RedisDataSource redis) {
        this.redis = redis;
    }
}
