package com.chord.framework.boot.autoconfigure.common;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultScriptExecutor;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Created on 2020/7/6
 *
 * @author: wulinfeng
 */
public class RedisTemplateFactory {

    public static RedisTemplate create(RedisConnectionFactory redisConnectionFactory) {

        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setDefaultSerializer(stringRedisSerializer);
        redisTemplate.setValueSerializer(stringRedisSerializer);
        redisTemplate.setScriptExecutor(new DefaultScriptExecutor(redisTemplate));

        return redisTemplate;

    }

}
