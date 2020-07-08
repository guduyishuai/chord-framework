package com.chord.framework.boot.autoconfigure.security.captcha;

import com.chord.framework.boot.autoconfigure.common.RedisTemplateFactory;
import com.chord.framework.boot.autoconfigure.security.captcha.conditional.ConditionalOnCaptcha;
import com.chord.framework.security.SecurityProperties;
import io.lettuce.core.RedisClient;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.Jedis;

/**
 * Created on 2020/7/6
 *
 * @author: wulinfeng
 */
@ConditionalOnCaptcha
@Configuration
public class RedisCaptchaAutoConfiguration {

    @Configuration
    @ConditionalOnClass({GenericObjectPool.class, JedisConnection.class, Jedis.class })
    static class JedisCaptchaConfiguration extends AbstractJedisCaptchaAutoConfiguration {

        public JedisCaptchaConfiguration(SecurityProperties properties) {
            super(properties);
        }

        @Bean
        @ConditionalOnMissingBean(name = "captchaRedisTemplate")
        public RedisTemplate captchaRedisTemplate() {
            return RedisTemplateFactory.create(captchaRedisConnectionFactory());
        }

        @Bean
        @ConditionalOnMissingBean(name = "captchaRedisConnectionFactory")
        public JedisConnectionFactory captchaRedisConnectionFactory() {
            return createJedisConnectionFactory();
        }

    }

    @Configuration
    @ConditionalOnClass(RedisClient.class)
    static class LettuceCaptchaConfiguration extends AbstractLettuceCaptchaAutoConfiguration {

        public LettuceCaptchaConfiguration(SecurityProperties properties) {
            super(properties);
        }

        @Bean
        @ConditionalOnMissingBean(name = "captchaRedisTemplate")
        public RedisTemplate captchaRedisTemplate() {
            return RedisTemplateFactory.create(captchaRedisConnectionFactory(captchaLettuceClientResources()));
        }

        @Bean(destroyMethod = "shutdown")
        @ConditionalOnMissingBean(name = "captchaLettuceClientResources")
        public DefaultClientResources captchaLettuceClientResources() {
            return DefaultClientResources.create();
        }

        @Bean
        @ConditionalOnMissingBean(name = "captchaRedisConnectionFactory")
        public LettuceConnectionFactory captchaRedisConnectionFactory(
                ClientResources lettuceCaptchaClientResources) {
            LettuceClientConfiguration clientConfig = getLettuceClientConfiguration(
                    lettuceCaptchaClientResources, this.properties.getLettuce().getPool());
            return createLettuceConnectionFactory(clientConfig);
        }

    }

}
