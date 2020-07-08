package com.chord.framework.boot.autoconfigure.mutex.lock.properties;

import javax.validation.constraints.NotEmpty;

/**
 * Created on 2020/6/28
 *
 * @author: wulinfeng
 */
public class RedissionDataSource {

    @NotEmpty
    private String config;

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }
}
