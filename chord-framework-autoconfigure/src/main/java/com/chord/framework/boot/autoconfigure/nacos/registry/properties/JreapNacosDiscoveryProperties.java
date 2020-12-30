package com.chord.framework.boot.autoconfigure.nacos.registry.properties;

import com.alibaba.cloud.nacos.registry.NacosServiceRegistry;
import com.alibaba.nacos.api.common.Constants;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 *
 * 用于分组注册
 * 目前官方已经支持分组注册
 *
 * @see NacosServiceRegistry
 *
 * Created on 2020/8/21
 *
 * @author: wulinfeng
 */
@Deprecated
@ConfigurationProperties(JreapNacosDiscoveryProperties.PREFIX)
public class JreapNacosDiscoveryProperties {

    public static final String PREFIX = "jreap.nacos.discovery";

    private String group = Constants.DEFAULT_GROUP;

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
