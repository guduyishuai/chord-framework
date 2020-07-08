package com.chord.framework.security.captcha;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

/**
 * Created on 2020/7/6
 *
 * @author: wulinfeng
 */
public class CaptchaTokenGranterCreator {

    private final RedisTemplate<String, String> captchaRedisTemplate;

    public CaptchaTokenGranterCreator(RedisTemplate<String, String> redisTemplate) {
        this.captchaRedisTemplate = redisTemplate;
    }

    public AbstractCaptchaTokenGranter create(String keyPrefix, CaptchaCodeResolver captchaCodeResolver,
                                              AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService,
                                              OAuth2RequestFactory requestFactory, String grantType
                                              ) {
        return new CaptchaTokenGranter(captchaRedisTemplate, keyPrefix, captchaCodeResolver, tokenServices, clientDetailsService, requestFactory, grantType);
    }

}
