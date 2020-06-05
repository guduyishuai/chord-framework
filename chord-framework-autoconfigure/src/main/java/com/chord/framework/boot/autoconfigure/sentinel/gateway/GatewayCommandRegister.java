package com.chord.framework.boot.autoconfigure.sentinel.gateway;

import com.alibaba.csp.sentinel.command.CommandHandler;
import com.chord.framework.boot.autoconfigure.sentinel.common.CommandRegister;
import com.chord.framework.boot.autoconfigure.sentinel.core.CommandRegistionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 *
 * 类似于{@link CommandRegister}
 *
 * Created on 2020/5/20
 *
 * @author: wulinfeng
 */
public class GatewayCommandRegister {

    private static final Logger logger = LoggerFactory.getLogger(CommandRegister.class);

    private static final String UPDATE_GATEWAY_RULE_COMMAND_HANDLER_KEY = "gateway/updateRules";
    private static final String UPDATE_GATEWAY_API_DEFINITION_GROUP_COMMAND_HANDLER_KEY = "gateway/updateApiDefinitions";

    public static void registerCommands(Map<String, CommandHandler> handlerMap) throws IllegalAccessException {
        if (handlerMap != null) {
            handlerMap.remove(UPDATE_GATEWAY_RULE_COMMAND_HANDLER_KEY);
            handlerMap.remove(UPDATE_GATEWAY_API_DEFINITION_GROUP_COMMAND_HANDLER_KEY);
            for (Map.Entry<String, CommandHandler> e : handlerMap.entrySet()) {
                CommandRegistionUtils.registerCommand(e.getKey(), e.getValue());
            }
            CommandRegistionUtils.registerCommand(UPDATE_GATEWAY_RULE_COMMAND_HANDLER_KEY, new ConsistencyUpdateGatewayRuleCommandHandler());
            CommandRegistionUtils.registerCommand(UPDATE_GATEWAY_API_DEFINITION_GROUP_COMMAND_HANDLER_KEY, new ConsistencyUpdateGatewayApiDefinitionGroupCommandHandler());
        }
        logger.debug("替换UpdateGatewayRuleCommandHandler->ConsistencyUpdateGatewayRuleCommandHandler");
        logger.debug("替换UpdateGatewayApiDefinitionGroupCommandHandler->ConsistencyGatewayApiDefinitionGroupCommandHandler");
    }

}
