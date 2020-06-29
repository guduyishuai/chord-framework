package com.chord.framework.sentinel.gateway;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;

/**
 * Created on 2020/5/20
 *
 * @author: wulinfeng
 */
public enum UrlMatchStategy {

    EXACT(SentinelGatewayConstants.PARAM_MATCH_STRATEGY_EXACT),
    PREFIX(SentinelGatewayConstants.PARAM_MATCH_STRATEGY_PREFIX),
    REGEX(SentinelGatewayConstants.PARAM_MATCH_STRATEGY_REGEX),
    CONTAINS(SentinelGatewayConstants.PARAM_MATCH_STRATEGY_CONTAINS);

    private final int strategy;

    UrlMatchStategy(int strategy) {
        this.strategy = strategy;
    }

    public int getStrategy() {
        return strategy;
    }

}
