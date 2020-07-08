package com.chord.framework.security.captcha;

import lombok.Data;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import javax.validation.constraints.NotEmpty;

/**
 * Created on 2020/7/6
 *
 * @author: wulinfeng
 */
@Data
public class CaptchaProperties {

    @NestedConfigurationProperty
    private RedisProperties connection;

    private boolean enable = false;

    @NotEmpty
    private String keyPrefix = "chord_captcha_";

}
