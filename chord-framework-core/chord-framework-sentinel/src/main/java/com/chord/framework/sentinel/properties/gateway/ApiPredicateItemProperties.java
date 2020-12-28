package com.chord.framework.sentinel.properties.gateway;

import com.chord.framework.sentinel.gateway.UrlMatchStategy;

/**
 * Created on 2020/5/20
 *
 * @author: wulinfeng
 */
public class ApiPredicateItemProperties {

    private String pattern;

    private UrlMatchStategy stategy = UrlMatchStategy.EXACT;

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public UrlMatchStategy getStategy() {
        return stategy;
    }

    public void setStategy(UrlMatchStategy stategy) {
        this.stategy = stategy;
    }
}
