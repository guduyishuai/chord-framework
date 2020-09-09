package com.chord.framework.metrics;

import io.micrometer.core.instrument.Tag;

import javax.management.ObjectName;
import java.util.function.BiConsumer;

/**
 *
 * 从jmx中获取监控，注册到通用的micrometer
 *
 * Created on 2020/7/24
 *
 * @author: wulinfeng
 */
public interface JmxMetricsAdapter {

    /**
     *
     * 从jmx中获取监控，注册到通用的micrometer
     *
     * @param objName 监控对象名
     * @param key 监控的key
     * @param value 监控的value
     * @param perObject 实现注册到通用的micrometer
     */
    void registerMetrics(String objName, String key, String value, BiConsumer<ObjectName, Iterable<Tag>> perObject);

}
