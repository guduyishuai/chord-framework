package com.chord.framework.boot.autoconfigure.security.captcha;

import com.chord.framework.boot.autoconfigure.security.captcha.conditional.ConditionalOnCaptcha;
import com.chord.framework.security.SecurityProperties;
import com.chord.framework.security.captcha.BaseCaptchaCodeResolver;
import com.chord.framework.security.captcha.CaptchaCodeResolver;
import com.chord.framework.security.captcha.CaptchaTokenGranterCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

/**
 * Created on 2020/7/6
 *
 * @author: wulinfeng
 */
@ConditionalOnCaptcha
@Configuration
public class CaptchaAutoConfiguration {

    @Resource(name = "captchaRedisTemplate")
    private RedisTemplate<String, String> captchaRedisTemplate;

    @Bean
    @ConditionalOnMissingBean
    public CaptchaCodeResolver captchaCodeResolver() {
        return new BaseCaptchaCodeResolver();
    }

    @Bean
    public CaptchaTokenGranterCreator captchaTokenGranterCreator() {
        return new CaptchaTokenGranterCreator(captchaRedisTemplate);
    }

}
