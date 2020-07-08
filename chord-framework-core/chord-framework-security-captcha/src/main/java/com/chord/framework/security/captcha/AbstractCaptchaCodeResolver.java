package com.chord.framework.security.captcha;

import org.springframework.security.oauth2.provider.TokenRequest;

import java.util.Map;

/**
 * Created on 2020/7/6
 *
 * @author: wulinfeng
 */
public abstract class AbstractCaptchaCodeResolver implements CaptchaCodeResolver {

    @Override
    public String resolve(TokenRequest tokenRequest) {
        Map<String, String> params = tokenRequest.getRequestParameters();
        if(params == null) {
            return null;
        }
        return params.get(getParamKey());
    }

    protected abstract String getParamKey();

}
