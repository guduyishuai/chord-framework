package com.chord.framework.mutex.lock.zookeeper;

import lombok.Data;
import org.apache.curator.RetryPolicy;

/**
 * Created on 2020/6/23
 *
 * @author: wulinfeng
 */
@Data
public class ZookeeperConfiguration {

    private String connectString;
    private int sessionTimeoutMs;
    private int connectionTimeoutMs;
    private String lockPath;
    private RetryPolicy retryPolicy;

}
