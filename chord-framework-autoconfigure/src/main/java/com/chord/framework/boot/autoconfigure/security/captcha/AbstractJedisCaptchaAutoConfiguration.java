package com.chord.framework.boot.autoconfigure.security.captcha;

import com.chord.framework.boot.autoconfigure.common.redis.AbstractJedisAutoConfiguration;
import com.chord.framework.security.SecurityProperties;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;

/**
 * Created on 2020/7/6
 *
 * @author: wulinfeng
 */
public class AbstractJedisCaptchaAutoConfiguration extends AbstractJedisAutoConfiguration<SecurityProperties> {

    public AbstractJedisCaptchaAutoConfiguration(SecurityProperties securityProperties) {
        super(securityProperties);
    }

    @Override
    protected RedisProperties getRedisProperties(SecurityProperties properties) {
        return RedisPropertiesResolver.resolve(properties);
    }

}
