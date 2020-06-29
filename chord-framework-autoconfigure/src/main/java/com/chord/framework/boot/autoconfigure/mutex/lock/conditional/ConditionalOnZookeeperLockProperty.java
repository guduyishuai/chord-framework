package com.chord.framework.boot.autoconfigure.mutex.lock.conditional;

import com.chord.framework.boot.autoconfigure.mutex.lock.properties.MutexLockProperties;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

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
@Conditional(ConditionalOnZookeeperLockProperty.OnZookeeperLockPropertyConditional.class)
public @interface ConditionalOnZookeeperLockProperty {

    class OnZookeeperLockPropertyConditional implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            String connectString = context.getEnvironment().getProperty(MutexLockProperties.PREFIX + ".zookeeper.connectString");
            return !StringUtils.isEmpty(connectString);
        }

    }

}
