package com.chord.framework.boot.autoconfigure.sentinel.common;

import com.alibaba.csp.sentinel.command.CommandHandler;
import com.chord.framework.boot.autoconfigure.sentinel.core.CommandRegistionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 *
 * 将setRules，setParamFlowRules注册成ConsistencyModifyRuleCommandHandler，而不是ModifyRuleCommandHandler
 * ConsistencyModifyParamFlowRuleCommandHandler，而不是ModifyFlowRuleCommandHandler
 *
 * Created on 2020/5/15
 *
 * @author: wulinfeng
 */
public class CommandRegister {

    private static final Logger logger = LoggerFactory.getLogger(CommandRegister.class);

    private static final String MODIFY_RULE_COMMAND_HANDLER_KEY = "setRules";
    private static final String MODIFY_PARAM_RULE_COMMAND_HANDLER_KEY = "setParamFlowRules";

    public static void registerCommands(Map<String, CommandHandler> handlerMap) throws IllegalAccessException {
        if (handlerMap != null) {
            handlerMap.remove(MODIFY_RULE_COMMAND_HANDLER_KEY);
            handlerMap.remove(MODIFY_PARAM_RULE_COMMAND_HANDLER_KEY);
            for (Map.Entry<String, CommandHandler> e : handlerMap.entrySet()) {
                CommandRegistionUtils.registerCommand(e.getKey(), e.getValue());
            }
            CommandRegistionUtils.registerCommand(MODIFY_RULE_COMMAND_HANDLER_KEY, new ConsistencyModifyRuleCommandHandler());
            CommandRegistionUtils.registerCommand(MODIFY_PARAM_RULE_COMMAND_HANDLER_KEY, new ConsistencyModifyParamFlowRuleCommandHandler());
        }
        logger.debug("替换ModifyRuleCommandHandler->ConsistencyModifyRuleCommandHandler");
        logger.debug("替换ModifyFlowRuleCommandHandler->ConsistencyModifyParamFlowRuleCommandHandler");
    }

}
