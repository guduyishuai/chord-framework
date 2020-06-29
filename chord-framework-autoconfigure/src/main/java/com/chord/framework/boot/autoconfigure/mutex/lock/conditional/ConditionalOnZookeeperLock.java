package com.chord.framework.boot.autoconfigure.mutex.lock.conditional;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created on 2020/6/29
 *
 * @author: wulinfeng
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Conditional(ConditionalOnZookeeperLock.OnZookeeperLockConditional.class)
public @interface ConditionalOnZookeeperLock {

    class OnZookeeperLockConditional extends AllNestedConditions {

        public OnZookeeperLockConditional() {
            super(ConfigurationPhase.PARSE_CONFIGURATION);
        }

        @ConditionalOnClass({ZooKeeper.class, CuratorFramework.class})
        static class FoundClass {

        }

        @ConditionalOnZookeeperLockProperty
        static class FoundProperty {

        }

    }

}
