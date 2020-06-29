package com.chord.framework.sentinel.gateway;

import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.command.UpdateGatewayApiDefinitionGroupCommandHandler;
import com.alibaba.csp.sentinel.command.CommandRequest;
import com.alibaba.csp.sentinel.command.CommandResponse;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chord.framework.sentinel.common.ConsistencyModifyRulesCommandHandler;
import com.chord.framework.sentinel.core.WriteableDataSourceUtils;

import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Set;

import static com.chord.framework.sentinel.gateway.GatewayWritableDataSourceRegistry.getGatewayApiDefinitionGroupDataSource;

/**
 *
 * 类似{@link ConsistencyModifyRulesCommandHandler}
 *
 * Created on 2020/5/20
 *
 * @author: wulinfeng
 */
public class ConsistencyUpdateGatewayApiDefinitionGroupCommandHandler extends UpdateGatewayApiDefinitionGroupCommandHandler {

    @Override
    public CommandResponse<String> handle(CommandRequest request) {
        String data = request.getParam("data");
        if (StringUtil.isBlank(data)) {
            return CommandResponse.ofFailure(new IllegalArgumentException("Bad data"));
        }
        try {
            data = URLDecoder.decode(data, "utf-8");
        } catch (Exception e) {
            RecordLog.info("Decode gateway API definition data error", e);
            return CommandResponse.ofFailure(e, "decode gateway API definition data error");
        }

        RecordLog.info("[API Server] Receiving data change (type: gateway API definition): {0}", data);

        String result = SUCCESS_MSG;

        if(WriteableDataSourceUtils.writeToDataSource(getGatewayApiDefinitionGroupDataSource(), data)) {
            Set<ApiDefinition> apiDefinitions = parseJson(data);
            GatewayApiDefinitionManager.loadApiDefinitions(apiDefinitions);
        } else {
            result = WRITE_DS_FAILURE_MSG;
        }

        return CommandResponse.ofSuccess(result);
    }

    private static final String SUCCESS_MSG = "success";
    private static final String WRITE_DS_FAILURE_MSG = "partial success (write data source failed)";

    /**
     * Parse json data to set of {@link ApiDefinition}.
     *
     * Since the predicateItems of {@link ApiDefinition} is set of interface,
     * here we parse predicateItems to {@link ApiPathPredicateItem} temporarily.
     */
    private Set<ApiDefinition> parseJson(String data) {
        Set<ApiDefinition> apiDefinitions = new HashSet<>();
        JSONArray array = JSON.parseArray(data);
        for (Object obj : array) {
            JSONObject o = (JSONObject)obj;
            ApiDefinition apiDefinition = new ApiDefinition((o.getString("apiName")));
            Set<ApiPredicateItem> predicateItems = new HashSet<>();
            JSONArray itemArray = o.getJSONArray("predicateItems");
            if (itemArray != null) {
                predicateItems.addAll(itemArray.toJavaList(ApiPathPredicateItem.class));
            }
            apiDefinition.setPredicateItems(predicateItems);
            apiDefinitions.add(apiDefinition);
        }

        return apiDefinitions;
    }

}
