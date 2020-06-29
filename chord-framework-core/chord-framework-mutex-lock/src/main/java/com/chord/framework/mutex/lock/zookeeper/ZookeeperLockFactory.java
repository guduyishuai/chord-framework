package com.chord.framework.mutex.lock.zookeeper;

import com.chord.framework.mutex.lock.Lock;
import com.chord.framework.mutex.lock.LockFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

/**
 * Created on 2020/6/23
 *
 * @author: wulinfeng
 */
public class ZookeeperLockFactory implements LockFactory {

    public static final int DEFAULT_SESSION_TIMEOUT_MS = Integer.getInteger("curator-default-session-timeout", 60000);
    public static final int DEFAULT_CONNECTION_TIMEOUT_MS = Integer.getInteger("curator-default-connection-timeout", 15000);

    private final ZookeeperConfiguration zookeeperConfiguration;

    private final CuratorFramework curatorFramework;

    private final String lockPath;

    public ZookeeperLockFactory(ZookeeperConfiguration zookeeperConfiguration) throws UnsupportedEncodingException {
        this.zookeeperConfiguration = zookeeperConfiguration;
        this.lockPath = zookeeperConfiguration.getLockPath();
        this.curatorFramework = buildClient();
    }

    @Override
    public Lock create(String name) {
        return new ZookeeperLock(new InterProcessMutex(curatorFramework, lockPath), name);
    }

    public CuratorFramework getCuratorFramework() {
        return curatorFramework;
    }

    private CuratorFramework buildClient() throws UnsupportedEncodingException {
        String connectString;
        int sessionTimeoutMs;
        int connectionTimeoutMs;

        if(StringUtils.isEmpty(zookeeperConfiguration.getConnectString())) {
            connectString = new String(CuratorFrameworkFactory.getLocalAddress(), "UTF-8");
        } else {
            connectString = zookeeperConfiguration.getConnectString();
        }

        if(zookeeperConfiguration.getSessionTimeoutMs() == 0) {
            sessionTimeoutMs = DEFAULT_SESSION_TIMEOUT_MS;
        } else {
            sessionTimeoutMs = zookeeperConfiguration.getSessionTimeoutMs();
        }

        if(zookeeperConfiguration.getConnectionTimeoutMs() == 0) {
            connectionTimeoutMs = DEFAULT_CONNECTION_TIMEOUT_MS;
        } else {
            connectionTimeoutMs = zookeeperConfiguration.getConnectionTimeoutMs();
        }

        return CuratorFrameworkFactory.newClient(
                connectString,
                sessionTimeoutMs,
                connectionTimeoutMs,
                Optional.ofNullable(zookeeperConfiguration.getRetryPolicy())
                        .orElse(new ExponentialBackoffRetry(1000, 3)));
    }
}
