package com.chord.framework.security.base;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Created on 2020/7/1
 *
 * @author: wulinfeng
 */
public interface UserDetailsLoader {

    UserDetails load(String username, PasswordEncoder passwordEncoder);

}
