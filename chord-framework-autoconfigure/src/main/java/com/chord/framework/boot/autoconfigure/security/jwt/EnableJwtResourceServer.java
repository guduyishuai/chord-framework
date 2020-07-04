package com.chord.framework.boot.autoconfigure.security.jwt;

import com.chord.framework.security.base.SecurityConfiguration;
import com.chord.framework.security.jwt.JwtTokenStoreConfiguration;
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
@Import({ResourceServerConfiguration.class, SecurityConfiguration.class, JwtTokenStoreConfiguration.class})
public @interface EnableJwtResourceServer {
}
