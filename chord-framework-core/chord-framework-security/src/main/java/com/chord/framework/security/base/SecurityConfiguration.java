package com.chord.framework.security.base;

import com.chord.framework.security.SecurityProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2020/7/1
 *
 * @author: wulinfeng
 */
@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private SecurityProperties securityProperties;

    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    @ConditionalOnMissingBean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder,
                                                 UserDetailsLoader userDetailsLoader) {
        return new BaseUserDetailsService(passwordEncoder, userDetailsLoader);
    }

    @Bean
    @ConditionalOnMissingBean
    public UserDetailsLoader userDetailsLoader() {
        return (username, passwordEncoder) ->  {
            List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
            GrantedAuthority grantedAuthority = new SimpleGrantedAuthority("ROLE_CHORD");
            grantedAuthorities.add(grantedAuthority);
            return new User("admin", passwordEncoder.encode("123456"), grantedAuthorities);
        };
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 非basic授权，表单方式
        // 所有请求需要身份认证
        http.formLogin()
                .loginProcessingUrl(securityProperties.getLoginAccess())
                .and()
                .authorizeRequests()
                .antMatchers(
                        securityProperties.getLoginAccess(),
                        "/swagger-resources/**",
                        "/v2/api-docs",
                        "/webjars/**",
                        "/oauth/check_token").permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .csrf().disable();
    }

}
