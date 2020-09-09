package com.chord.framework.nacos.config.handler;

import org.springframework.core.Ordered;

import java.util.Optional;

/**
 *
 * 默认处理器，不进行任何处理
 *
 * Created on 2020/8/17
 *
 * @author: wulinfeng
 */
public class DefaultConfigPropertyHandler implements ConfigPropertyHandler {

    private int order = Ordered.HIGHEST_PRECEDENCE;

    @Override
    public String handle(String key, String plainOriginText, String originTest, String preText) {
        if(Optional.ofNullable(originTest).orElse("").equals(preText)) {
            return originTest;
        } else {
            return preText;
        }
    }

    @Override
    public int getOrder() {
        return order;
    }
}
