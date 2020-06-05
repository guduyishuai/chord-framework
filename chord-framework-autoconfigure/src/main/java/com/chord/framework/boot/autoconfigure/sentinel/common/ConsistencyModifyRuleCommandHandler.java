package com.chord.framework.boot.autoconfigure.sentinel.common;

import com.alibaba.csp.sentinel.command.CommandRequest;
import com.alibaba.csp.sentinel.command.CommandResponse;
import com.alibaba.csp.sentinel.command.handler.ModifyRulesCommandHandler;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSONArray;
import com.chord.framework.boot.autoconfigure.sentinel.core.WriteableDataSourceUtils;

import java.net.URLDecoder;
import java.util.List;

import static com.alibaba.csp.sentinel.transport.util.WritableDataSourceRegistry.*;

/**
 *
 * 由于ModifyRulesCommandHandler先刷新内存，再写数据源，会造成数据不一致，这里把顺序调整了
 * 由于不需要通过ServiceLoader加载，因为setRules名称会被ModifyRulesCommandHandler覆盖，所以不需要通过CommandMapping注解提供name
 *
 * 热点规则配置没有在该类中处理，而是在{@link ConsistencyModifyParamFlowRuleCommandHandler}中处理
 *
 * @see ConsistencyModifyParamFlowRuleCommandHandler
 *
 * Created on 2020/5/15
 *
 * @author: wulinfeng
 */
public class ConsistencyModifyRuleCommandHandler extends ModifyRulesCommandHandler {

    @Override
    public CommandResponse<String> handle(CommandRequest request) {
        String type = request.getParam("type");
        // rule data in get parameter
        String data = request.getParam("data");
        if (StringUtil.isNotEmpty(data)) {
            try {
                data = URLDecoder.decode(data, "utf-8");
            } catch (Exception e) {
                RecordLog.info("Decode rule data error", e);
                return CommandResponse.ofFailure(e, "decode rule data error");
            }
        }

        RecordLog.info(String.format("Receiving rule change (type: %s): %s", type, data));

        String result = "success";

        if (FLOW_RULE_TYPE.equalsIgnoreCase(type)) {
            List<FlowRule> flowRules = JSONArray.parseArray(data, FlowRule.class);
            if (WriteableDataSourceUtils.writeToDataSource(getFlowDataSource(), flowRules)) {
                FlowRuleManager.loadRules(flowRules);
            } else {
                result = WRITE_DS_FAILURE_MSG;
            }
            return CommandResponse.ofSuccess(result);
        } else if (AUTHORITY_RULE_TYPE.equalsIgnoreCase(type)) {
            List<AuthorityRule> rules = JSONArray.parseArray(data, AuthorityRule.class);
            if (WriteableDataSourceUtils.writeToDataSource(getAuthorityDataSource(), rules)) {
                AuthorityRuleManager.loadRules(rules);
            } else {
                result = WRITE_DS_FAILURE_MSG;
            }
            return CommandResponse.ofSuccess(result);
        } else if (DEGRADE_RULE_TYPE.equalsIgnoreCase(type)) {
            List<DegradeRule> rules = JSONArray.parseArray(data, DegradeRule.class);
            if (WriteableDataSourceUtils.writeToDataSource(getDegradeDataSource(), rules)) {
                DegradeRuleManager.loadRules(rules);
            } else {
                result = WRITE_DS_FAILURE_MSG;
            }
            return CommandResponse.ofSuccess(result);
        } else if (SYSTEM_RULE_TYPE.equalsIgnoreCase(type)) {
            List<SystemRule> rules = JSONArray.parseArray(data, SystemRule.class);
            if (WriteableDataSourceUtils.writeToDataSource(getSystemSource(), rules)) {
                SystemRuleManager.loadRules(rules);
            } else {
                result = WRITE_DS_FAILURE_MSG;
            }
            return CommandResponse.ofSuccess(result);
        }
        return CommandResponse.ofFailure(new IllegalArgumentException("invalid type"));
    }

    private static final String WRITE_DS_FAILURE_MSG = "partial success (write data source failed)";
    private static final String FLOW_RULE_TYPE = "flow";
    private static final String DEGRADE_RULE_TYPE = "degrade";
    private static final String SYSTEM_RULE_TYPE = "system";
    private static final String AUTHORITY_RULE_TYPE = "authority";

}
