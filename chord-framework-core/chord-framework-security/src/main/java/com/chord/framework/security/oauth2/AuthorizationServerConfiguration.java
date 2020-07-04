package com.chord.framework.security.oauth2;

import com.chord.framework.security.ClientProperties;
import com.chord.framework.security.SecurityProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.builders.ClientDetailsServiceBuilder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created on 2020/6/30
 *
 * @author: wulinfeng
 */
@Configuration
@EnableConfigurationProperties(SecurityProperties.class)
@EnableAuthorizationServer
public class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter implements ApplicationContextAware {

    protected ApplicationContext applicationContext;

    @Autowired
    private SecurityProperties securityProperties;

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) {

        // 认证通过才能访问/oauth/token_key
        // 不需要认证可以访问/oauth/check_token
        // 使用ClientCredentialsTokenEndpointFilter
        security.tokenKeyAccess(securityProperties.getTokenKeyAccess())
                .checkTokenAccess(securityProperties.getCheckTokenAccess())
                .allowFormAuthenticationForClients()
                .passwordEncoder(
                        Optional.ofNullable(applicationContext.getBean(PasswordEncoder.class))
                            .orElse(new BCryptPasswordEncoder()));

        // 403处理
        try {
            AccessDeniedHandler accessDeniedHandler = applicationContext.getBean(AccessDeniedHandler.class);
            security.accessDeniedHandler(accessDeniedHandler);
        } catch (NoSuchBeanDefinitionException e) {

        }

        // 401处理
        try {
            AuthenticationEntryPoint authenticationEntryPoint = applicationContext.getBean(AuthenticationEntryPoint.class);
            security.authenticationEntryPoint(authenticationEntryPoint);
        } catch (NoSuchBeanDefinitionException e) {

        }

        // 自定义filter，用来对TokenEndpoint的请求进行处理
        Map<String, TokenEndpointAuthenticationFilter> filterMap = applicationContext.getBeansOfType(TokenEndpointAuthenticationFilter.class);
        if(filterMap != null) {
            filterMap.forEach((key, value) -> {
                security.addTokenEndpointAuthenticationFilter(value);
            });
        }

        // ssl
        if(securityProperties.isSslOnly()) {
            security.sslOnly();
        }

        // 通过objcetPostProcessor进行更多属性的设置
        Map<String, OAuth2ObjectPostProcessor> objectPostProcessorMap = applicationContext.getBeansOfType(OAuth2ObjectPostProcessor.class);
        if(objectPostProcessorMap != null) {
            objectPostProcessorMap.forEach((key, value) -> {
                security.addObjectPostProcessor(value);
            });
        }

    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {

        ClientDetailsServiceBuilder builder;

        if(securityProperties.getClient().isInMemory()) {
            builder = clients.inMemory();

            if(securityProperties.getClient().getClients() != null) {
                for (ClientProperties.ClientInfo clientInfo : securityProperties.getClient().getClients()) {
                    builder.withClient(clientInfo.getClientId())
                            .secret(Optional.ofNullable(applicationContext.getBean(PasswordEncoder.class))
                                    .orElse(new BCryptPasswordEncoder()).encode(clientInfo.getClientSecret()))
                            .accessTokenValiditySeconds(clientInfo.getAccessTokenValiditySeconds())
                            .refreshTokenValiditySeconds(clientInfo.getRefreshTokenValidtySecnods())
                            .authorizedGrantTypes(clientInfo.getAuthorizedGrantTypes())
                            .scopes(clientInfo.getScopes())
                            .additionalInformation(clientInfo.getAdditionalInformation())
                            .authorities(clientInfo.getAuthorities())
                            // 只适合于授权码模式
                            .autoApprove(clientInfo.isAutoApprove())
                            .autoApprove(clientInfo.getApproveScopes());
                }
            }
        } else {
            DataSource dataSource = applicationContext.getBean(OAuth2DataSource.class);
            if(dataSource == null) {
                dataSource = applicationContext.getBean(DataSource.class);
            }
            clients.jdbc(dataSource);
        }

    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {

        TokenStore tokenStore = applicationContext.getBean(TokenStore.class);
        Map<String, TokenEnhancer> tokenEnhancers = applicationContext.getBeansOfType(TokenEnhancer.class);
        AuthenticationManager authenticationManager = applicationContext.getBean(AuthenticationManager.class);
        UserDetailsService userDetailsService = applicationContext.getBean(UserDetailsService.class);

        // 认证信息保存在token中，通过该接口进行转换
        AccessTokenConverter accessTokenConverter = applicationContext.getBean(AccessTokenConverter.class);

        endpoints.tokenStore(tokenStore)
                .authenticationManager(authenticationManager)
                .userDetailsService(userDetailsService)
                .accessTokenConverter(accessTokenConverter)
                .allowedTokenEndpointRequestMethods(securityProperties.getEndpoints().getHttpMethod());


        if(tokenEnhancers != null) {
            TokenEnhancerChain enhancerChain = new TokenEnhancerChain();
            List<TokenEnhancer> enhancers = new ArrayList<>();
            tokenEnhancers.forEach((key, value) -> enhancers.add(value));
            enhancerChain.setTokenEnhancers(enhancers);
            endpoints.tokenEnhancer(enhancerChain);
        }

        // 授权码模式，批准操作的存储
        if(securityProperties.getEndpoints().isApprovalStoreDisabled()) {
            endpoints.approvalStoreDisabled();
        } else {
            try {
                ApprovalStore approvalStore = applicationContext.getBean(ApprovalStore.class);
                endpoints.approvalStore(approvalStore);
            } catch (NoSuchBeanDefinitionException e) {

            }
        }

        // 授权码模式，处理授权码
        try {
            AuthorizationCodeServices authorizationCodeServices = applicationContext.getBean(AuthorizationCodeServices.class);
            endpoints.authorizationCodeServices(authorizationCodeServices);
        } catch (NoSuchBeanDefinitionException e) {

        }

        // 异常处理，返回合适的格式
        try {
            WebResponseExceptionTranslator exceptionTranslator = applicationContext.getBean(WebResponseExceptionTranslator.class);
            endpoints.exceptionTranslator(exceptionTranslator);
        } catch (NoSuchBeanDefinitionException e) {

        }

        // filter处理完，也就是认证成功后，允许通过HandlerInterceptor做一些事情
        Map<String, HandlerInterceptor> handlerInterceptors = applicationContext.getBeansOfType(HandlerInterceptor.class);

        if(handlerInterceptors != null) {
            handlerInterceptors.forEach((key, value) -> {
                endpoints.addInterceptor(value);
            });
        }

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
