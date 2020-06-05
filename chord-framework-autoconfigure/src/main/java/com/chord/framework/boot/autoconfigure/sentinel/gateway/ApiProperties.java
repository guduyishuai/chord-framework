package com.chord.framework.boot.autoconfigure.sentinel.gateway;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.Map;

/**
 *
 * 对ApiDefinition提供配置项
 *
 * Created on 2020/5/20
 *
 * @author: wulinfeng
 */
@ConfigurationProperties(prefix = ApiProperties.PREFIX)
public class ApiProperties {

    public static final String PREFIX = "chord.sentinel.gateway.api";

    private boolean enableNacos = false;

    @NestedConfigurationProperty
    private Map<String, ApiDefinitionProperties> definition;

    public boolean isEnableNacos() {
        return enableNacos;
    }

    public void setEnableNacos(boolean enableNacos) {
        this.enableNacos = enableNacos;
    }

    public Map<String, ApiDefinitionProperties> getDefinition() {
        return definition;
    }

    public void setDefinition(Map<String, ApiDefinitionProperties> definition) {
        this.definition = definition;
    }

}
