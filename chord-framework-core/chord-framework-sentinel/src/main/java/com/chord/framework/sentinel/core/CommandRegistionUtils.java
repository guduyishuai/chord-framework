package com.chord.framework.sentinel.core;

import com.alibaba.csp.sentinel.command.CommandHandler;
import com.alibaba.csp.sentinel.log.CommandCenterLog;
import com.alibaba.csp.sentinel.transport.command.SimpleHttpCommandCenter;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.chord.framework.sentinel.common.Constants;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Created on 2020/5/20
 *
 * @author: wulinfeng
 */
public class CommandRegistionUtils {

    public static void registerCommand(String commandName, CommandHandler handler) throws IllegalAccessException {
        if (StringUtil.isEmpty(commandName)) {
            return;
        }

        Field field = ReflectionUtils.findField(SimpleHttpCommandCenter.class, Constants.PROPERTY_HANDLER_MAP);
        field.setAccessible(true);
        Map<String, CommandHandler> handlerMap = (Map<String, CommandHandler>) field.get(null);

        if (handlerMap.containsKey(commandName)) {
            CommandCenterLog.warn("Register failed (duplicate command): " + commandName);
            return;
        }

        handlerMap.put(commandName, handler);
    }

}
