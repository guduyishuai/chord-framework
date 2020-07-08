package com.chord.framework.boot.autoconfigure.security.captcha;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Created on 2020/7/6
 *
 * @author: wulinfeng
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({RedisCaptchaAutoConfiguration.class, CaptchaAutoConfiguration.class})
public @interface EnableCaptcha {

}
