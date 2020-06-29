package com.chord.framework.boot.autoconfigure.mutex.lock;

import com.chord.framework.boot.autoconfigure.common.ValidationUtils;
import com.chord.framework.boot.autoconfigure.mutex.lock.conditional.ConditionalOnRedisLock;
import com.chord.framework.boot.autoconfigure.mutex.lock.properties.MutexLockProperties;
import com.chord.framework.boot.autoconfigure.mutex.lock.properties.RedisDataSource;
import com.chord.framework.mutex.lock.redis.LockValueGenerator;
import com.chord.framework.mutex.lock.redis.RedisConfiguration;
import com.chord.framework.mutex.lock.redis.RedisLockFactory;
import io.lettuce.core.RedisClient;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultScriptExecutor;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;

import java.net.UnknownHostException;
import java.time.Duration;
import java.util.UUID;

/**
 * Created on 2020/6/29
 *
 * @author: wulinfeng
 */
@Configuration
@ConditionalOnRedisLock
public class RedisLockAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "redisLockTemplate")
    public RedisTemplate redisLockTemplate(RedisConnectionFactory redisConnectionFactory) {

        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setDefaultSerializer(stringRedisSerializer);
        redisTemplate.setValueSerializer(stringRedisSerializer);
        redisTemplate.setScriptExecutor(new DefaultScriptExecutor(redisTemplate));
        return redisTemplate;

    }

    @Bean
    public RedisConfiguration redisConfiguration(MutexLockProperties properties, LockValueGenerator lockValueGenerator) {
        if(properties.getRedis() == null) {
            throw new IllegalArgumentException("not found the config for redis");
        }

        RedisDataSource redisDataSource = properties.getRedis();
        new ValidationUtils<RedisDataSource>().validate(redisDataSource);

        RedisConfiguration redisConfiguration = new RedisConfiguration();
        redisConfiguration.setLockKey(redisDataSource.getLockKey());
        redisConfiguration.setLockExpireTime(redisDataSource.getLockExpireTime());
        redisConfiguration.setLockValueGenerator(lockValueGenerator);

        return redisConfiguration;
    }

    @Bean
    @ConditionalOnMissingBean
    public LockValueGenerator lockValueGenerator() {
        return ()-> UUID.randomUUID().toString();
    }

    @Bean
    public RedisLockFactory redisLockFactory(RedisTemplate redisLockTemplate, RedisConfiguration redisConfiguration) {
        return new RedisLockFactory(redisLockTemplate, redisConfiguration);
    }

    @Configuration
    @ConditionalOnClass({GenericObjectPool.class, JedisConnection.class, Jedis.class })
    static class JedisLockConfiguration extends AbstractRedisLockAutoConfiguration {

        public JedisLockConfiguration(MutexLockProperties properties) {
            super(properties);
        }

        @Bean
        @ConditionalOnMissingBean(RedisConnectionFactory.class)
        public JedisConnectionFactory redisConnectionFactory() {
            return createJedisConnectionFactory();
        }

        private JedisConnectionFactory createJedisConnectionFactory() {
            JedisClientConfiguration clientConfiguration = getJedisClientConfiguration();
            if (getSentinelConfig() != null) {
                return new JedisConnectionFactory(getSentinelConfig(), clientConfiguration);
            }
            if (getClusterConfiguration() != null) {
                return new JedisConnectionFactory(getClusterConfiguration(),
                        clientConfiguration);
            }
            return new JedisConnectionFactory(getStandaloneConfig(), clientConfiguration);
        }

        private JedisClientConfiguration getJedisClientConfiguration() {
            JedisClientConfiguration.JedisClientConfigurationBuilder builder = applyProperties(
                    JedisClientConfiguration.builder());
            RedisProperties.Pool pool = this.properties.getJedis().getPool();
            if (pool != null) {
                applyPooling(pool, builder);
            }
            return builder.build();
        }

        private JedisClientConfiguration.JedisClientConfigurationBuilder applyProperties(
                JedisClientConfiguration.JedisClientConfigurationBuilder builder) {
            if (this.properties.isSsl()) {
                builder.useSsl();
            }
            if (this.properties.getTimeout() != null) {
                Duration timeout = this.properties.getTimeout();
                builder.readTimeout(timeout).connectTimeout(timeout);
            }
            return builder;
        }

        private void applyPooling(RedisProperties.Pool pool,
                                  JedisClientConfiguration.JedisClientConfigurationBuilder builder) {
            builder.usePooling().poolConfig(jedisPoolConfig(pool));
        }

        private JedisPoolConfig jedisPoolConfig(RedisProperties.Pool pool) {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(pool.getMaxActive());
            config.setMaxIdle(pool.getMaxIdle());
            config.setMinIdle(pool.getMinIdle());
            if (pool.getMaxWait() != null) {
                config.setMaxWaitMillis(pool.getMaxWait().toMillis());
            }
            return config;
        }

    }

    @Configuration
    @ConditionalOnMissingClass({
            "org.apache.commons.pool2.impl.GenericObjectPool",
            "org.springframework.data.redis.connection.jedis.JedisConnection",
            "redis.clients.jedis.Jedis"})
    @ConditionalOnClass(RedisClient.class)
    static class LettuceLockConfiguration extends AbstractRedisLockAutoConfiguration {

        public LettuceLockConfiguration(MutexLockProperties properties) {
            super(properties);
        }

        @Bean(destroyMethod = "shutdown")
        @ConditionalOnMissingBean(ClientResources.class)
        public DefaultClientResources lettuceClientResources() {
            return DefaultClientResources.create();
        }

        @Bean
        @ConditionalOnMissingBean(RedisConnectionFactory.class)
        public LettuceConnectionFactory redisConnectionFactory(
                ClientResources clientResources) {
            LettuceClientConfiguration clientConfig = getLettuceClientConfiguration(
                    clientResources, this.properties.getLettuce().getPool());
            return createLettuceConnectionFactory(clientConfig);
        }

        private LettuceClientConfiguration getLettuceClientConfiguration(
                ClientResources clientResources, RedisProperties.Pool pool) {
            LettuceClientConfiguration.LettuceClientConfigurationBuilder builder = createBuilder(pool);
            applyProperties(builder);
            builder.clientResources(clientResources);
            return builder.build();
        }

        private LettuceClientConfiguration.LettuceClientConfigurationBuilder createBuilder(RedisProperties.Pool pool) {
            if (pool == null) {
                return LettuceClientConfiguration.builder();
            }
            return new PoolBuilderFactory().createBuilder(pool);
        }

        private LettuceClientConfiguration.LettuceClientConfigurationBuilder applyProperties(
                LettuceClientConfiguration.LettuceClientConfigurationBuilder builder) {
            if (this.properties.isSsl()) {
                builder.useSsl();
            }
            if (this.properties.getTimeout() != null) {
                builder.commandTimeout(this.properties.getTimeout());
            }
            if (this.properties.getLettuce() != null) {
                RedisProperties.Lettuce lettuce = this.properties.getLettuce();
                if (lettuce.getShutdownTimeout() != null
                        && !lettuce.getShutdownTimeout().isZero()) {
                    builder.shutdownTimeout(
                            this.properties.getLettuce().getShutdownTimeout());
                }
            }
            return builder;
        }

        private LettuceConnectionFactory createLettuceConnectionFactory(
                LettuceClientConfiguration clientConfiguration) {
            if (getSentinelConfig() != null) {
                return new LettuceConnectionFactory(getSentinelConfig(), clientConfiguration);
            }
            if (getClusterConfiguration() != null) {
                return new LettuceConnectionFactory(getClusterConfiguration(),
                        clientConfiguration);
            }
            return new LettuceConnectionFactory(getStandaloneConfig(), clientConfiguration);
        }

        private static class PoolBuilderFactory {

            public LettuceClientConfiguration.LettuceClientConfigurationBuilder createBuilder(RedisProperties.Pool properties) {
                return LettucePoolingClientConfiguration.builder()
                        .poolConfig(getPoolConfig(properties));
            }

            private GenericObjectPoolConfig getPoolConfig(RedisProperties.Pool properties) {
                GenericObjectPoolConfig config = new GenericObjectPoolConfig();
                config.setMaxTotal(properties.getMaxActive());
                config.setMaxIdle(properties.getMaxIdle());
                config.setMinIdle(properties.getMinIdle());
                if (properties.getMaxWait() != null) {
                    config.setMaxWaitMillis(properties.getMaxWait().toMillis());
                }
                return config;
            }

        }

    }

}
