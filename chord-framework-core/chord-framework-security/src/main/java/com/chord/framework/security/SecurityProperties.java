package com.chord.framework.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

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

    private ClientProperties client = new ClientProperties();

    private EndpointsProperties endpoints = new EndpointsProperties();

    private String jwtSigningKey = "chord";

    private String loginPage = "/oauth/token";

    private String loginAccess = "/oauth/token";

}
