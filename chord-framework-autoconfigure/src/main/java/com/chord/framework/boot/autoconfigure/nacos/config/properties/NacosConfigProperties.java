package com.chord.framework.boot.autoconfigure.nacos.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created on 2020/8/13
 *
 * @author: wulinfeng
 */
@ConfigurationProperties("chord.nacos.config")
public class NacosConfigProperties {

    private String prefix;

    private String defaultApplicationName = "application";

    private String defaultProfile = "default";

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getDefaultApplicationName() {
        return defaultApplicationName;
    }

    public void setDefaultApplicationName(String defaultApplicationName) {
        this.defaultApplicationName = defaultApplicationName;
    }

    public String getDefaultProfile() {
        return defaultProfile;
    }

    public void setDefaultProfile(String defaultProfile) {
        this.defaultProfile = defaultProfile;
    }
}
