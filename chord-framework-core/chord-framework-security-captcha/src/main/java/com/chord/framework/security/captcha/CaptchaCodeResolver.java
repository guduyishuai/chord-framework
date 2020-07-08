package com.chord.framework.security.captcha;

import org.springframework.security.oauth2.provider.TokenRequest;

/**
 * Created on 2020/7/7
 *
 * @author: wulinfeng
 */
public interface CaptchaCodeResolver {

    String resolve(TokenRequest tokenRequest);

}
