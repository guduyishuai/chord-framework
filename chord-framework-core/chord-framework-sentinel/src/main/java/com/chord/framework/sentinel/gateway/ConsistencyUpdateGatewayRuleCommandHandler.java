package com.chord.framework.sentinel.gateway;

import com.alibaba.csp.sentinel.adapter.gateway.common.command.UpdateGatewayRuleCommandHandler;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.command.CommandRequest;
import com.alibaba.csp.sentinel.command.CommandResponse;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSONArray;
import com.chord.framework.sentinel.common.ConsistencyModifyRulesCommandHandler;
import com.chord.framework.sentinel.core.WriteableDataSourceUtils;

import java.net.URLDecoder;
import java.util.HashSet;
import java.util.List;

import static com.chord.framework.sentinel.gateway.GatewayWritableDataSourceRegistry.getGatewayFlowDataSource;

/**
 *
 * 类似{@link ConsistencyModifyRulesCommandHandler}
 *
 * Created on 2020/5/20
 *
 * @author: wulinfeng
 */
public class ConsistencyUpdateGatewayRuleCommandHandler extends UpdateGatewayRuleCommandHandler {

    @Override
    public CommandResponse<String> handle(CommandRequest request) {
        String data = request.getParam("data");
        if (StringUtil.isBlank(data)) {
            return CommandResponse.ofFailure(new IllegalArgumentException("Bad data"));
        }
        try {
            data = URLDecoder.decode(data, "utf-8");
        } catch (Exception e) {
            RecordLog.info("Decode gateway rule data error", e);
            return CommandResponse.ofFailure(e, "decode gateway rule data error");
        }

        RecordLog.info(String.format("[API Server] Receiving rule change (type: gateway rule): %s", data));

        String result = SUCCESS_MSG;
        List<GatewayFlowRule> flowRules = JSONArray.parseArray(data, GatewayFlowRule.class);

        if(WriteableDataSourceUtils.writeToDataSource(getGatewayFlowDataSource(), flowRules)) {
            GatewayRuleManager.loadRules(new HashSet<>(flowRules));
        } else {
            result = WRITE_DS_FAILURE_MSG;
        }

        return CommandResponse.ofSuccess(result);
    }

    private static final String SUCCESS_MSG = "success";
    private static final String WRITE_DS_FAILURE_MSG = "partial success (write data source failed)";

}
