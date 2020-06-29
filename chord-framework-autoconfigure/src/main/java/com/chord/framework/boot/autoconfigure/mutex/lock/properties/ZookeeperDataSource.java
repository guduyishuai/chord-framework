package com.chord.framework.boot.autoconfigure.mutex.lock.properties;

import com.chord.framework.mutex.lock.zookeeper.ZookeeperLockFactory;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Created on 2020/6/28
 *
 * @author: wulinfeng
 */
public class ZookeeperDataSource {

    @NotEmpty
    private String connectString;

    @NotNull
    private Integer sessionTimeoutMs = ZookeeperLockFactory.DEFAULT_SESSION_TIMEOUT_MS;

    @NotNull
    private Integer connectionTimeoutMs = ZookeeperLockFactory.DEFAULT_CONNECTION_TIMEOUT_MS;

    @NotEmpty
    private String lockPath = "/chord/lock";

    public String getConnectString() {
        return connectString;
    }

    public void setConnectString(String connectString) {
        this.connectString = connectString;
    }

    public Integer getSessionTimeoutMs() {
        return sessionTimeoutMs;
    }

    public void setSessionTimeoutMs(Integer sessionTimeoutMs) {
        this.sessionTimeoutMs = sessionTimeoutMs;
    }

    public Integer getConnectionTimeoutMs() {
        return connectionTimeoutMs;
    }

    public void setConnectionTimeoutMs(Integer connectionTimeoutMs) {
        this.connectionTimeoutMs = connectionTimeoutMs;
    }

    public String getLockPath() {
        return lockPath;
    }

    public void setLockPath(String lockPath) {
        this.lockPath = lockPath;
    }

}
