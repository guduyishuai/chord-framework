package com.chord.framework.security.oauth2;

import com.chord.framework.security.SecurityProperties;
import com.chord.framework.security.authority.AuthorityService;
import com.chord.framework.security.authority.ChordFilterSecurityMetadataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * Created on 2020/7/1
 *
 * @author: wulinfeng
 */
@Configuration
@EnableConfigurationProperties(SecurityProperties.class)
@EnableResourceServer
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

    @Autowired
    private SecurityProperties securityProperties;

    @Autowired(required = false)
    private AuthorityService authorityService;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        ExpressionUrlAuthorizationConfigurer.ExpressionInterceptUrlRegistry
                registry =
                    http.requestMatcher(new OAuthRequestedMatcher())
                            .formLogin()
                            .loginPage(securityProperties.getLoginPage())
                            .and()
                            .csrf().disable()
                            .authorizeRequests()
                            .antMatchers(
                                    securityProperties.getLoginAccess(),
                                    "/oauth/check_token",
                                    "/swagger-resources/**",
                                    "/v2/api-docs",
                                    "/webjars/**",
                                    "/oauth/check_token"
                            ).permitAll()
                            .anyRequest()
                            .authenticated();

        if(authorityService != null) {
            registry.withObjectPostProcessor(new ObjectPostProcessor<FilterSecurityInterceptor>() {

                @Override
                public <O extends FilterSecurityInterceptor> O postProcess(O interceptor) {
                    interceptor.setSecurityMetadataSource(new ChordFilterSecurityMetadataSource(authorityService));
                    interceptor.setAccessDecisionManager(new AffirmativeBased(Arrays.asList(new RoleVoter())));
                    return interceptor;
                }

            });
        }
    }

    private static class OAuthRequestedMatcher implements RequestMatcher {

        @Override
        public boolean matches(HttpServletRequest request) {
            AntPathRequestMatcher swaggerRequestMatcher = new AntPathRequestMatcher("/swagger-ui.html");
            EndpointRequest.EndpointRequestMatcher endpointRequestMatcher = EndpointRequest.toAnyEndpoint();
            return !swaggerRequestMatcher.matches(request) && !endpointRequestMatcher.matches(request);
        }

    }

}
