package com.chord.framework.sentinel.core;

import com.alibaba.csp.sentinel.command.CommandHandler;
import com.alibaba.csp.sentinel.init.InitFunc;
import com.alibaba.csp.sentinel.init.InitOrder;
import com.alibaba.csp.sentinel.transport.command.SimpleHttpCommandCenter;
import com.chord.framework.sentinel.common.CommandRegister;
import com.chord.framework.sentinel.common.ConsistencyModifyParamFlowRulesCommandHandler;
import com.chord.framework.sentinel.common.ConsistencyModifyRulesCommandHandler;
import com.chord.framework.sentinel.common.Constants;
import com.chord.framework.sentinel.gateway.ConsistencyUpdateGatewayApiDefinitionGroupCommandHandler;
import com.chord.framework.sentinel.gateway.ConsistencyUpdateGatewayRuleCommandHandler;
import org.springframework.util.ReflectionUtils;

import java.util.Map;

/**
 *
 * 之间是使用ByteBuddy重新替换{@link SimpleHttpCommandCenter}类，实现以下CommandHandler的替换
 * {@link ConsistencyModifyParamFlowRulesCommandHandler}替换为{@link com.alibaba.csp.sentinel.command.handler.ModifyParamFlowRulesCommandHandler}
 * {@link ConsistencyModifyRulesCommandHandler}替换为{@link com.alibaba.csp.sentinel.command.handler.ModifyRulesCommandHandler}
 * {@link ConsistencyUpdateGatewayRuleCommandHandler}替换为{@link com.alibaba.csp.sentinel.adapter.gateway.common.command.UpdateGatewayRuleCommandHandler}
 * {@link ConsistencyUpdateGatewayApiDefinitionGroupCommandHandler}替换为{@link com.alibaba.csp.sentinel.adapter.gateway.common.command.UpdateGatewayApiDefinitionGroupCommandHandler}
 *
 * 但是ByteBuddy不能使用openjdk，而且修改字节码的方式不太好，因此修改为InitFunc的方式
 *
 * Created on 2020/6/5
 *
 * @author: wulinfeng
 */
@InitOrder(0)
public class CommandCenterReplaceInitFunc implements InitFunc {

    @Override
    public void init() throws Exception {
        ReflectionUtils.doWithLocalFields(
                SimpleHttpCommandCenter.class,
                field -> {
                    if(Constants.PROPERTY_HANDLER_MAP.equals(field.getName())) {
                        field.setAccessible(true);
                        CommandRegister.registerCommands((Map<String, CommandHandler>) field.get(null));
                    }
                });
    }

}
