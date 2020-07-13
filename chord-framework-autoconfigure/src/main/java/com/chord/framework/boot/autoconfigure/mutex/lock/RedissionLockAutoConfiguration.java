package com.chord.framework.boot.autoconfigure.mutex.lock;

import com.chord.framework.boot.autoconfigure.mutex.lock.conditional.ConditionalOnRedissionLock;
import com.chord.framework.boot.autoconfigure.mutex.lock.properties.MutexLockProperties;
import com.chord.framework.boot.autoconfigure.mutex.lock.properties.RedissionDataSource;
import com.chord.framework.mutex.lock.LockFactory;
import com.chord.framework.mutex.lock.redisson.RedissionConfiguration;
import com.chord.framework.mutex.lock.redisson.RedissionLockFactory;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Created on 2020/6/29
 *
 * @author: wulinfeng
 */
@ConditionalOnRedissionLock
@Configuration
public class RedissionLockAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(RedissionLockAutoConfiguration.class);

    @Autowired
    private RedisProperties redisProperties;

    @Autowired
    private MutexLockProperties mutexLockProperties;

    @Autowired(required = false)
    private List<RedissonAutoConfigurationCustomizer> redissonAutoConfigurationCustomizers;

    @Autowired
    private ApplicationContext ctx;

    @Bean
    @ConditionalOnMissingBean
    public LockFactory redissionLockFactory() {
        RedissonClient redissonClient = createRedisson(getConfig());
        return new RedissionLockFactory(redissonClient);
    }

    @Bean
    public RedissionConfiguration redissionConfiguration() {
        RedissionConfiguration redissionConfiguration = new RedissionConfiguration();
        redissionConfiguration.setConfig(getConfig());
        return redissionConfiguration;
    }

    private Config getConfig() {

        if(mutexLockProperties.getRedission() == null) {
            throw new IllegalArgumentException("not found the config for redission");
        }

        RedissionDataSource redissionDataSource = mutexLockProperties.getRedission();
        if(StringUtils.isEmpty(redissionDataSource.getConfig())) {
            throw new IllegalArgumentException("not found the config for redission");
        }

        Config config;
        Method clusterMethod = ReflectionUtils.findMethod(RedisProperties.class, "getCluster");
        Method timeoutMethod = ReflectionUtils.findMethod(RedisProperties.class, "getTimeout");
        if(timeoutMethod == null) {
            logger.error("timeout cannot acquired");
            throw new IllegalArgumentException("timeout cannot acquired");
        }
        Object timeoutValue = ReflectionUtils.invokeMethod(timeoutMethod, redisProperties);
        int timeout;
        if(null == timeoutValue){
            timeout = 10000;
        }else if (!(timeoutValue instanceof Integer)) {
            Method millisMethod = ReflectionUtils.findMethod(timeoutValue.getClass(), "toMillis");
            if(millisMethod == null) {
                logger.error("timeout convert to ms failed");
                throw new IllegalArgumentException("timeout convert to ms failed");
            }
            timeout = Optional.ofNullable(ReflectionUtils.invokeMethod(millisMethod, timeoutValue))
                    .map(result -> ((Long) result).intValue())
                    .orElse(0);
        } else {
            timeout = (Integer)timeoutValue;
        }

        if (redissionDataSource.getConfig() != null) {
            try {
                InputStream is = getConfigStream(redissionDataSource.getConfig());
                config = Config.fromJSON(is);
            } catch (IOException e) {
                // trying next format
                try {
                    InputStream is = getConfigStream(redissionDataSource.getConfig());
                    config = Config.fromYAML(is);
                } catch (IOException e1) {
                    throw new IllegalArgumentException("Can't parse config", e1);
                }
            }
        } else if (redisProperties.getSentinel() != null) {
            Method nodesMethod = ReflectionUtils.findMethod(RedisProperties.Sentinel.class, "getNodes");
            if(nodesMethod == null) {
                logger.error("the nodes of sentinel cannot be acquired");
                throw new IllegalArgumentException("the nodes of sentinel cannot be acquired");
            }
            Object nodesValue = ReflectionUtils.invokeMethod(nodesMethod, redisProperties.getSentinel());

            String[] nodes;
            if (nodesValue instanceof String) {
                nodes = convert(Arrays.asList(((String)nodesValue).split(",")));
            } else {
                if(nodesValue == null) {
                    logger.error("the nodes of sentinel cannot be acquired");
                    throw new IllegalArgumentException("the nodes of sentinel cannot be acquired");
                }
                nodes = convert((List<String>) nodesValue);
            }

            config = new Config();
            config.useSentinelServers()
                    .setMasterName(redisProperties.getSentinel().getMaster())
                    .addSentinelAddress(nodes)
                    .setDatabase(redisProperties.getDatabase())
                    .setConnectTimeout(timeout)
                    .setPassword(redisProperties.getPassword());
        } else if (clusterMethod != null && ReflectionUtils.invokeMethod(clusterMethod, redisProperties) != null) {
            Object clusterObject = ReflectionUtils.invokeMethod(clusterMethod, redisProperties);
            if(clusterObject == null) {
                logger.error("cluster cannot be acquired");
                throw new IllegalArgumentException("cluster cannot be acquired");
            }
            Method nodesMethod = ReflectionUtils.findMethod(clusterObject.getClass(), "getNodes");
            if(nodesMethod == null) {
                logger.error("the nodes of sentinel cannot be acquired");
                throw new IllegalArgumentException("the nodes of sentinel cannot be acquired");
            }
            List<String> nodesObject = (List) ReflectionUtils.invokeMethod(nodesMethod, clusterObject);
            if(nodesObject == null) {
                logger.error("the nodes of cluster cannot be acquired");
                throw new IllegalArgumentException("the nodes of cluster cannot be acquired");
            }
            String[] nodes = convert(nodesObject);

            config = new Config();
            config.useClusterServers()
                    .addNodeAddress(nodes)
                    .setConnectTimeout(timeout)
                    .setPassword(redisProperties.getPassword());
        } else {
            config = new Config();
            String prefix = "redis://";
            Method method = ReflectionUtils.findMethod(RedisProperties.class, "isSsl");
            if (method != null && (Boolean)ReflectionUtils.invokeMethod(method, redisProperties)) {
                prefix = "rediss://";
            }

            config.useSingleServer()
                    .setAddress(prefix + redisProperties.getHost() + ":" + redisProperties.getPort())
                    .setConnectTimeout(timeout)
                    .setDatabase(redisProperties.getDatabase())
                    .setPassword(redisProperties.getPassword());
        }

        return config;

    }

    private RedissonClient createRedisson(Config config) {
        if (redissonAutoConfigurationCustomizers != null) {
            for (RedissonAutoConfigurationCustomizer customizer : redissonAutoConfigurationCustomizers) {
                customizer.customize(config);
            }
        }
        return Redisson.create(config);
    }

    private String[] convert(List<String> nodesObject) {
        List<String> nodes = new ArrayList<>(nodesObject.size());
        for (String node : nodesObject) {
            if (!node.startsWith("redis://") && !node.startsWith("rediss://")) {
                nodes.add("redis://" + node);
            } else {
                nodes.add(node);
            }
        }
        return nodes.toArray(new String[nodes.size()]);
    }

    private InputStream getConfigStream(String config) throws IOException {
        Resource resource = ctx.getResource(config);
        InputStream is = resource.getInputStream();
        return is;
    }

}
