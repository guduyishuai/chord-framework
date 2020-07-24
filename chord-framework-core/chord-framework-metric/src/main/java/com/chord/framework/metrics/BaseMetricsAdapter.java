package com.chord.framework.metrics;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;

/**
 * Created on 2020/7/24
 *
 * @author: wulinfeng
 */
public class BaseMetricsAdapter implements MetricsAdapter {

    private static final Logger logger = LoggerFactory.getLogger(BaseMetricsAdapter.class);

    protected final Iterable<Tag> tags;

    protected final MBeanServer mBeanServer;

    public BaseMetricsAdapter() {
        this(Tags.empty());
    }

    public BaseMetricsAdapter(Iterable<Tag> tags) {
        this.tags = tags;
        this.mBeanServer = getMBeanServer();
    }

    @Override
    public void registerMetrics(String objName, String key, String value, BiConsumer<ObjectName, Iterable<Tag>> perObject) {

        // 已经注册了MBean
        try {
            // 获得MBean
            Set<ObjectName> objectNames = mBeanServer.queryNames(new ObjectName(objName + ":" + key + "=" + value + ",*"), null);
            if(!objectNames.isEmpty()) {
                objectNames.forEach(objectName -> {
                    perObject.accept(objectName, Tags.concat(tags, nameTag(objectName)));
                });
                return;
            }
        } catch (MalformedObjectNameException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("Error registering Kafka JMX based metrics", e);
        }

        // 没有注册MBean
        NotificationListener notificationListener = (notification, handback) -> {
            // MBean注册后的回调
            MBeanServerNotification mbs = (MBeanServerNotification) notification;
            ObjectName obj = mbs.getMBeanName();
            perObject.accept(obj, Tags.concat(tags, nameTag(obj)));
        };

        NotificationFilter filter = (NotificationFilter) notification -> {
            // 只监听注册的消息
            if (!MBeanServerNotification.REGISTRATION_NOTIFICATION.equals(notification.getType())) {
                return false;
            }

            // 只监听满足条件的MBean注册消息
            ObjectName obj = ((MBeanServerNotification) notification).getMBeanName();
            return obj.getDomain().equals(objName) && obj.getKeyProperty(key).equals(value);

        };

        try {
            // 添加注册监听
            mBeanServer.addNotificationListener(MBeanServerDelegate.DELEGATE_NAME, notificationListener, filter, null);
        } catch (InstanceNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("Error registering MBean listener", e);
        }

    }

    private MBeanServer getMBeanServer() {
        return Optional.ofNullable(MBeanServerFactory.findMBeanServer(null))
                .map(servers -> servers.get(0))
                .orElseGet(ManagementFactory::getPlatformMBeanServer);
    }

    private Iterable<Tag> nameTag(ObjectName name) {
        final String NAME = "name";
        if (name.getKeyProperty(NAME) != null) {
            return Tags.of(NAME, name.getKeyProperty(NAME).replaceAll("\"", ""));
        } else {
            return Collections.emptyList();
        }
    }

    protected double safeDouble(Callable<Object> callable) {
        try {
            return Double.parseDouble(callable.call().toString());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return 0.0;
        }
    }

}
