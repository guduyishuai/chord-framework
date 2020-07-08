package com.chord.framework.security.captcha;

/**
 * Created on 2020/7/6
 *
 * @author: wulinfeng
 */
public class BaseCaptchaCodeResolver extends AbstractCaptchaCodeResolver {

    private static String PARAM_KEY = "captchaCode";

    @Override
    protected String getParamKey() {
        return PARAM_KEY;
    }

}
