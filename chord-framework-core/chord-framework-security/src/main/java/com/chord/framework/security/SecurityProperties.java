package com.chord.framework.security;

import com.chord.framework.security.captcha.CaptchaProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Created on 2020/7/1
 *
 * @author: wulinfeng
 */
@Data
@ConfigurationProperties(prefix = SecurityProperties.PREFIX)
public class SecurityProperties {

    public static final String PREFIX = "chord.security";

    private boolean sslOnly = false;

    private String tokenKeyAccess = "isAuthenticated()";

    private String checkTokenAccess = "permitAll()";

    @NestedConfigurationProperty
    private ClientProperties client = new ClientProperties();

    @NestedConfigurationProperty
    private EndpointsProperties endpoints = new EndpointsProperties();

    private String jwtSigningKey = "chord";

    private String loginPage = "/oauth/token";

    private String loginAccess = "/oauth/token";

    @NestedConfigurationProperty
    private CaptchaProperties captcha;

}
