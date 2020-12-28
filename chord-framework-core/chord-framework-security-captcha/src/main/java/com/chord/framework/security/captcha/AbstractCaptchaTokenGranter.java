package com.chord.framework.security.captcha;

import com.chord.framework.commons.utils.IpResolver;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.util.StringUtils;

/**
 * Created on 2020/7/6
 *
 * @author: wulinfeng
 */
public abstract class AbstractCaptchaTokenGranter extends AbstractTokenGranter {

    protected RedisTemplate<String, String> redisTemplate;

    protected String keyPrefix;

    protected CaptchaCodeResolver captchaCodeResolver;

    protected AbstractCaptchaTokenGranter(
            RedisTemplate<String, String> redisTemplate, String keyPrefix, CaptchaCodeResolver captchaCodeResolver,
            AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory, String grantType) {
        super(tokenServices, clientDetailsService, requestFactory, grantType);
        this.redisTemplate = redisTemplate;
        this.keyPrefix = keyPrefix;
        this.captchaCodeResolver = captchaCodeResolver;
    }

    @Override
    public OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {

        String code;
        try {
            code = redisTemplate.opsForValue().get(keyPrefix + IpResolver.getIp());
        } catch (RedisConnectionFailureException e) {
            throw new InvalidGrantException("redis连接失败");
        }

        if(StringUtils.isEmpty(code)) {
            throw new InvalidGrantException("验证码失效");
        }

        String requestCode = captchaCodeResolver.resolve(tokenRequest);

        if(StringUtils.isEmpty(requestCode)) {
            throw new InvalidGrantException("验证码为空");
        }

        if(!code.equalsIgnoreCase(requestCode)) {
            throw new InvalidGrantException("验证码不匹配");
        }

        OAuth2Request storedOAuth2Request = getRequestFactory().createOAuth2Request(client, tokenRequest);
        return new OAuth2Authentication(storedOAuth2Request, null);

    }

}
