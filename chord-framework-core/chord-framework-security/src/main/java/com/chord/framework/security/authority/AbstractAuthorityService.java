package com.chord.framework.security.authority;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created on 2020/7/7
 *
 * @author: wulinfeng
 */
public abstract class AbstractAuthorityService implements AuthorityService {

    @Override
    public LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> getAuthority() {

        List<AuthorityModel> authorityList = doGetAuthority();
        LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> authorities = new LinkedHashMap<>(16);
        for(AuthorityModel authority : authorityList) {
            RequestMatcher requestMatcher = createRequestMatcher(authority.getHttpMethod(), authority.getPartten());
            List<ConfigAttribute> configAttributes = createConfigAttribute(authority.getRoles());
            authorities.put(requestMatcher, configAttributes);
        }
        return authorities;

    }

    protected abstract List<AuthorityModel> doGetAuthority();

    protected RequestMatcher createRequestMatcher(String httpMethod, String antPatterns) {
        String method = httpMethod == null ? null : httpMethod;
        return new AntPathRequestMatcher(antPatterns, method);
    }

    protected List<ConfigAttribute> createConfigAttribute(List<String> roles) {
        List<ConfigAttribute> configAttributes = new ArrayList<>();
        for(String role : roles) {
            configAttributes.add(new SecurityConfig(role));
        }
        return configAttributes;
    }

}
