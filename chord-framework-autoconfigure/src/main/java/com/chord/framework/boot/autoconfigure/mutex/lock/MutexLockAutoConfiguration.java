package com.chord.framework.boot.autoconfigure.mutex.lock;

import com.chord.framework.boot.autoconfigure.mutex.lock.properties.MutexLockProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created on 2020/6/28
 *
 * @author: wulinfeng
 */
@EnableConfigurationProperties(MutexLockProperties.class)
@Configuration
public class MutexLockAutoConfiguration {
}
