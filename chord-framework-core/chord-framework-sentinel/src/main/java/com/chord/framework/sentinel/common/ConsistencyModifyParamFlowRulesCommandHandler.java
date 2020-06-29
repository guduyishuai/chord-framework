package com.chord.framework.sentinel.common;

import com.alibaba.csp.sentinel.command.CommandRequest;
import com.alibaba.csp.sentinel.command.CommandResponse;
import com.alibaba.csp.sentinel.command.handler.ModifyParamFlowRulesCommandHandler;
import com.alibaba.csp.sentinel.datasource.WritableDataSource;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSONArray;
import com.chord.framework.sentinel.core.WriteableDataSourceUtils;

import java.net.URLDecoder;
import java.util.List;

/**
 *
 * 类似{@link ConsistencyModifyRulesCommandHandler}
 *
 * Created on 2020/5/18
 *
 * @author: wulinfeng
 */
public class ConsistencyModifyParamFlowRulesCommandHandler extends ModifyParamFlowRulesCommandHandler {

    private static WritableDataSource<List<ParamFlowRule>> paramFlowWds = null;

    @Override
    public CommandResponse<String> handle(CommandRequest request) {
        String data = request.getParam("data");
        if (StringUtil.isBlank(data)) {
            return CommandResponse.ofFailure(new IllegalArgumentException("Bad data"));
        }
        try {
            data = URLDecoder.decode(data, "utf-8");
        } catch (Exception e) {
            RecordLog.info("Decode rule data error", e);
            return CommandResponse.ofFailure(e, "decode rule data error");
        }

        RecordLog.info(String.format("[API Server] Receiving rule change (type:parameter flow rule): %s", data));

        String result = SUCCESS_MSG;
        List<ParamFlowRule> flowRules = JSONArray.parseArray(data, ParamFlowRule.class);
        if(WriteableDataSourceUtils.writeToDataSource(paramFlowWds, flowRules)) {
            ParamFlowRuleManager.loadRules(flowRules);
        }
        else {
            result = WRITE_DS_FAILURE_MSG;
        }
        return CommandResponse.ofSuccess(result);
    }

    public synchronized static WritableDataSource<List<ParamFlowRule>> getWritableDataSource() {
        return paramFlowWds;
    }

    public synchronized static void setWritableDataSource(WritableDataSource<List<ParamFlowRule>> hotParamWds) {
        ConsistencyModifyParamFlowRulesCommandHandler.paramFlowWds = hotParamWds;
    }

    private static final String SUCCESS_MSG = "success";
    private static final String WRITE_DS_FAILURE_MSG = "partial success (write data source failed)";

}
