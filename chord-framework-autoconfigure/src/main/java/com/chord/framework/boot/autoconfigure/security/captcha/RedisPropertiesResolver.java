package com.chord.framework.boot.autoconfigure.security.captcha;

import com.chord.framework.security.SecurityProperties;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;

/**
 * Created on 2020/7/6
 *
 * @author: wulinfeng
 */
class RedisPropertiesResolver {

    static RedisProperties resolve(SecurityProperties properties) {
        if(properties.getCaptcha() == null) {
            throw new IllegalArgumentException("not found the config for redis");
        }
        return properties.getCaptcha().getConnection();
    }

}
