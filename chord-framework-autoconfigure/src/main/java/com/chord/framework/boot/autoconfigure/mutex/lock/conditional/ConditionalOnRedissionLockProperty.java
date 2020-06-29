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
@Conditional(ConditionalOnRedissionLockProperty.OnRedissionLockPropertyConditional.class)
public @interface ConditionalOnRedissionLockProperty {

    class OnRedissionLockPropertyConditional implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            String config = context.getEnvironment().getProperty(MutexLockProperties.PREFIX + ".redission.config");
            return !StringUtils.isEmpty(config);
        }

    }

}
