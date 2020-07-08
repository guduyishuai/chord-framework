package com.chord.framework.security.authority;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * Created on 2020/7/7
 *
 * @author: wulinfeng
 */
public interface AuthorityService {

    LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> getAuthority();

}
