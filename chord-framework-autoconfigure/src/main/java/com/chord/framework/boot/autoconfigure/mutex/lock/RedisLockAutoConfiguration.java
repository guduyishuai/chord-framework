package com.chord.framework.boot.autoconfigure.mutex.lock;

import com.chord.framework.boot.autoconfigure.common.RedisTemplateFactory;
import com.chord.framework.boot.autoconfigure.common.ValidationUtils;
import com.chord.framework.boot.autoconfigure.mutex.lock.conditional.ConditionalOnRedisLock;
import com.chord.framework.boot.autoconfigure.mutex.lock.properties.MutexLockProperties;
import com.chord.framework.boot.autoconfigure.mutex.lock.properties.RedisDataSource;
import com.chord.framework.mutex.lock.LockFactory;
import com.chord.framework.mutex.lock.redis.LockValueGenerator;
import com.chord.framework.mutex.lock.redis.RedisConfiguration;
import com.chord.framework.mutex.lock.redis.RedisLockFactory;
import io.lettuce.core.RedisClient;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultScriptExecutor;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.Jedis;

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
    public RedisConfiguration redisConfiguration(MutexLockProperties properties, LockValueGenerator lockValueGenerator) {
        if(properties.getRedis() == null) {
            throw new IllegalArgumentException("not found the config for redis");
        }

        RedisDataSource redisDataSource = properties.getRedis();

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
    public LockFactory redisLockFactory(RedisTemplate lockRedisTemplate, RedisConfiguration redisConfiguration) {
        return new RedisLockFactory(lockRedisTemplate, redisConfiguration);
    }

    @Configuration
    @ConditionalOnClass(Jedis.class)
    static class JedisLockConfiguration extends AbstractJedisLockAutoConfiguration {

        public JedisLockConfiguration(MutexLockProperties properties) {
            super(properties);
        }

        @Bean
        @ConditionalOnMissingBean(name = "lockRedisTemplate")
        public RedisTemplate lockRedisTemplate() {
            return RedisTemplateFactory.create(lockRedisConnectionFactory());
        }

        @Bean
        @ConditionalOnMissingBean(name = "lockRedisConnectionFactory")
        public JedisConnectionFactory lockRedisConnectionFactory() {
            return createJedisConnectionFactory();
        }

    }

    @Configuration
    @ConditionalOnClass(RedisClient.class)
    @ConditionalOnMissingClass({"redis.clients.jedis.Jedis"})
    static class LettuceLockConfiguration extends AbstractLettuceLockAutoConfiguration {

        public LettuceLockConfiguration(MutexLockProperties properties) {
            super(properties);
        }

        @Bean
        @ConditionalOnMissingBean(name = "lockRedisTemplate")
        public RedisTemplate lockRedisTemplate() {
            return RedisTemplateFactory.create(lockRedisConnectionFactory(lockLettuceClientResources()));
        }

        @Bean(destroyMethod = "shutdown")
        @ConditionalOnMissingBean(name = "lockLettuceClientResources")
        public DefaultClientResources lockLettuceClientResources() {
            return DefaultClientResources.create();
        }

        @Bean
        @ConditionalOnMissingBean(name = "lockRedisConnectionFactory")
        public LettuceConnectionFactory lockRedisConnectionFactory(
                ClientResources lettuceLockClientResources) {
            LettuceClientConfiguration clientConfig = getLettuceClientConfiguration(
                    lettuceLockClientResources, this.properties.getLettuce().getPool());
            return createLettuceConnectionFactory(clientConfig);
        }

    }

}
