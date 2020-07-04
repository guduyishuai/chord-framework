package com.chord.framework.boot.autoconfigure.security.oauth;

import com.chord.framework.security.base.SecurityConfiguration;
import com.chord.framework.security.oauth2.RedisTokenStoreConfiguration;
import com.chord.framework.security.oauth2.ResourceServerConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Created on 2020/7/1
 *
 * @author: wulinfeng
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({ResourceServerConfiguration.class, SecurityConfiguration.class, RedisTokenStoreConfiguration.class})
public @interface EnableOAuth2ResourceServer {
}
