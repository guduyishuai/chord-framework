package com.chord.framework.security.oauth2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

/**
 * Created on 2020/7/1
 *
 * @author: wulinfeng
 */
@Configuration
public class RedisTokenStoreConfiguration {

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Bean
    @ConditionalOnMissingBean
    public TokenStore redisTokenStore() {
        return new RedisTokenStore(redisConnectionFactory);
    }

}
