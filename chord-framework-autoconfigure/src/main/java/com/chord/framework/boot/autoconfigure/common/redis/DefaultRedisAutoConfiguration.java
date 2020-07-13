package com.chord.framework.boot.autoconfigure.common.redis;

import com.chord.framework.boot.autoconfigure.common.RedisTemplateFactory;
import io.lettuce.core.RedisClient;
import io.lettuce.core.resource.DefaultClientResources;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.Jedis;

/**
 * Created on 2020/7/13
 *
 * @author: wulinfeng
 */
@Configuration
public class DefaultRedisAutoConfiguration {

    @Configuration
    @ConditionalOnClass(Jedis.class)
    public static class JedisConfiguration extends AbstractJedisAutoConfiguration<RedisProperties> {

        @Autowired
        private RedisProperties redisProperties;

        public JedisConfiguration(RedisProperties redisProperties) {
            super(redisProperties);
        }

        @Bean
        @ConditionalOnMissingBean(name = "redisTemplate")
        public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
            return RedisTemplateFactory.create(redisConnectionFactory);
        }

        @Primary
        @Bean
        public JedisConnectionFactory jedisRedisConnectionFactory() {
            return createJedisConnectionFactory();
        }

        @Override
        protected RedisProperties getRedisProperties(RedisProperties redisProperties) {
            return redisProperties;
        }
    }

    @Configuration
    @ConditionalOnClass({RedisClient.class})
    @ConditionalOnMissingClass({"redis.clients.jedis.Jedis"})
    public static class LettuceConfiguration extends AbstractLettuceAutoConfiguration<RedisProperties> {

        @Autowired
        private RedisProperties redisProperties;

        public LettuceConfiguration(RedisProperties redisProperties) {
            super(redisProperties);
        }

        @Bean
        @ConditionalOnMissingBean(name = "redisTemplate")
        public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
            return RedisTemplateFactory.create(redisConnectionFactory);
        }

        @Bean(destroyMethod = "shutdown")
        public DefaultClientResources lettuceClientResources() {
            return DefaultClientResources.create();
        }

        @Primary
        @Bean
        public LettuceConnectionFactory lettuceRedisConnectionFactory() {
            LettuceClientConfiguration clientConfig = getLettuceClientConfiguration(
                    lettuceClientResources(), this.properties.getLettuce().getPool());
            return createLettuceConnectionFactory(clientConfig);
        }

        @Override
        protected RedisProperties getRedisProperties(RedisProperties redisProperties) {
            return redisProperties;
        }
    }

}
