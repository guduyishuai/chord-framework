package com.chord.framework.boot.autoconfigure.mutex.lock.properties;

import javax.validation.constraints.NotNull;

/**
 * Created on 2020/6/28
 *
 * @author: wulinfeng
 */
public class RedissionDataSource {

    @NotNull
    private String config;

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }
}
