package com.chord.framework.boot.autoconfigure.security.captcha;

import com.chord.framework.boot.autoconfigure.common.redis.AbstractLettuceAutoConfiguration;
import com.chord.framework.security.SecurityProperties;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;

/**
 * Created on 2020/7/6
 *
 * @author: wulinfeng
 */
public class AbstractLettuceCaptchaAutoConfiguration extends AbstractLettuceAutoConfiguration<SecurityProperties> {

    public AbstractLettuceCaptchaAutoConfiguration(SecurityProperties securityProperties) {
        super(securityProperties);
    }

    @Override
    protected RedisProperties getRedisProperties(SecurityProperties properties) {
        if(properties.getCaptcha() == null) {
            throw new IllegalArgumentException("not found the config for redis");
        }
        return properties.getCaptcha().getConnection();
    }

}
