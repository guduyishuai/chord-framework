package com.chord.framework.security.base;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Created on 2020/7/1
 *
 * @author: wulinfeng
 */
public class BaseUserDetailsService implements UserDetailsService {

    private PasswordEncoder passwordEncoder;

    private UserDetailsLoader userDetailsLoader;

    public BaseUserDetailsService(PasswordEncoder passwordEncoder, UserDetailsLoader userDetailsLoader) {
        this.passwordEncoder = passwordEncoder;
        this.userDetailsLoader = userDetailsLoader;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userDetailsLoader.load(username, passwordEncoder);
    }

}
