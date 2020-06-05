package com.chord.framework.boot.autoconfigure.sentinel.gateway;

import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.datasource.WritableDataSource;

import java.util.List;

/**
 *
 * 针对网关的WritableDataSource注册器
 *
 * Created on 2020/5/20
 *
 * @author: wulinfeng
 */
public class GatewayWritableDataSourceRegistry {

    private static WritableDataSource<List<GatewayFlowRule>> gatewayFlowDataSource = null;

    private static WritableDataSource<String> gatewayApiDefinitionGroupDataSource = null;

    private GatewayWritableDataSourceRegistry() {

    }

    public static synchronized void registerGatewayFlowDataSource(WritableDataSource<List<GatewayFlowRule>> datasource) {
        gatewayFlowDataSource = datasource;
    }

    public static WritableDataSource<List<GatewayFlowRule>> getGatewayFlowDataSource() {
        return gatewayFlowDataSource;
    }

    public static void registerGatewayApiDefinitionGroupDataSource(WritableDataSource<String> gatewayApiDefinitionGroupDataSource) {
        GatewayWritableDataSourceRegistry.gatewayApiDefinitionGroupDataSource = gatewayApiDefinitionGroupDataSource;
    }

    public static WritableDataSource<String> getGatewayApiDefinitionGroupDataSource() {
        return gatewayApiDefinitionGroupDataSource;
    }
}
