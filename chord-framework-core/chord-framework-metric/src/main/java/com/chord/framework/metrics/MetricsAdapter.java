package com.chord.framework.metrics;

import io.micrometer.core.instrument.Tag;

import javax.management.ObjectName;
import java.util.function.BiConsumer;

/**
 * Created on 2020/7/24
 *
 * @author: wulinfeng
 */
public interface MetricsAdapter {

    void registerMetrics(String objName, String key, String value, BiConsumer<ObjectName, Iterable<Tag>> perObject);

}
