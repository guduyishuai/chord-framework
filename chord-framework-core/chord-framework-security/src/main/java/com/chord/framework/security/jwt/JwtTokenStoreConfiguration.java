package com.chord.framework.security.jwt;

import com.chord.framework.security.SecurityProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.jwt.crypto.sign.MacSigner;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import javax.crypto.spec.SecretKeySpec;

/**
 * Created on 2020/7/1
 *
 * @author: wulinfeng
 */
@Configuration
public class JwtTokenStoreConfiguration {

    @Autowired
    private SecurityProperties securityProperties;

    @Bean
    @Primary
    public TokenStore jwtTokenStore() {
        return new JwtTokenStore(jwtAccessTokenConverter());
    }

    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();
        MacSigner macSigner = new MacSigner("HMACSHA256", new SecretKeySpec(securityProperties.getJwtSigningKey().getBytes(),"HMACSHA256"));
        jwtAccessTokenConverter.setSigner(macSigner);
        jwtAccessTokenConverter.setVerifier(macSigner);
        return jwtAccessTokenConverter;
    }

    @Bean
    @ConditionalOnMissingBean(name = "jwtTokenEnhancer")
    public TokenEnhancer jwtTokenEnhancer() {
        return (accessToken, authentication) -> accessToken;
    }

    @Bean
    @Primary
    public AuthorizationServerTokenServices tokenServices() {
        DefaultTokenServices tokenServices = new DefaultTokenServices();
        tokenServices.setTokenStore(jwtTokenStore());
        tokenServices.setTokenEnhancer(jwtTokenEnhancer());
        return tokenServices;
    }

}

