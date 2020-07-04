package com.chord.framework.security.jwt;

import com.chord.framework.security.SecurityProperties;
import com.chord.framework.security.oauth2.AuthorizationServerConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created on 2020/7/1
 *
 * @author: wulinfeng
 */
@Configuration
@EnableConfigurationProperties(SecurityProperties.class)
@EnableAuthorizationServer
public class JwtAuthorizationServerConfiguration extends AuthorizationServerConfiguration {

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {

        super.configure(endpoints);

        Map<String, TokenEnhancer> tokenEnhancers = applicationContext.getBeansOfType(TokenEnhancer.class);
        JwtAccessTokenConverter jwtAccessTokenConverter = applicationContext.getBean(JwtAccessTokenConverter.class);
        if(jwtAccessTokenConverter != null && tokenEnhancers != null) {
            TokenEnhancerChain enhancerChain = new TokenEnhancerChain();
            List<TokenEnhancer> enhancers = new ArrayList<>();
            tokenEnhancers.forEach((key, value) -> enhancers.add(value));
            enhancers.add(jwtAccessTokenConverter);
            enhancerChain.setTokenEnhancers(enhancers);
            endpoints.tokenEnhancer(enhancerChain)
                    .accessTokenConverter(jwtAccessTokenConverter);
        }

    }

}
