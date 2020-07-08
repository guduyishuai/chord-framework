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
public class CaptchaTokenGranter extends AbstractCaptchaTokenGranter {

    public CaptchaTokenGranter(
            RedisTemplate<String, String> redisTemplate, String keyPrefix, CaptchaCodeResolver imageCaptchaCodeResolver,
            AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory, String grantType) {
        super(redisTemplate, keyPrefix, imageCaptchaCodeResolver, tokenServices, clientDetailsService, requestFactory, grantType);
    }

}
