package com.chord.framework.mutex.lock.redisson;

import lombok.Data;
import org.redisson.config.Config;

/**
 * Created on 2020/6/23
 *
 * @author: wulinfeng
 */
@Data
public class RedissionConfiguration {

    private Config config;

}
