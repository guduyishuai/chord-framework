package com.chord.framework.boot.autoconfigure.sentinel.gateway;

import com.alibaba.csp.sentinel.command.CommandHandler;
import com.alibaba.csp.sentinel.init.InitFunc;
import com.alibaba.csp.sentinel.transport.command.SimpleHttpCommandCenter;
import com.chord.framework.boot.autoconfigure.sentinel.common.Constants;
import org.springframework.util.ReflectionUtils;

import java.util.Map;

/**
 * Created on 2020/6/5
 *
 * @author: wulinfeng
 */
public class GatewayCommandCenterReplaceInitFunc implements InitFunc {

    @Override
    public void init() throws Exception {

        ReflectionUtils.doWithLocalFields(
                SimpleHttpCommandCenter.class,
                field -> {
                    if(Constants.PROPERTY_HANDLER_MAP.equals(field.getName())) {
                        field.setAccessible(true);
                        GatewayCommandRegister.registerCommands((Map<String, CommandHandler>) field.get(null));
                    }
                });

    }

}
