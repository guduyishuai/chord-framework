package com.chord.framework.nacos.config.handler;

import org.springframework.core.Ordered;

/**
 *
 * 允许通过实现该接口对配置进行处理
 *
 * Created on 2020/8/17
 *
 * @author: wulinfeng
 */
public interface ConfigPropertyHandler extends Ordered {

    /**
     *
     * 处理配置信息
     *
     * @param key 键
     * @param plainOriginText 解密后的原始值
     * @param originTest 原始值
     * @param preText 上一个处理的值
     * @return
     */
    String handle(String key, String plainOriginText, String originTest, String preText);

}
