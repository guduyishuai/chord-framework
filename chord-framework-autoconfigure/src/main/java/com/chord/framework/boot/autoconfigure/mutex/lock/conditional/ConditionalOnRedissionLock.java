package com.chord.framework.boot.autoconfigure.mutex.lock.conditional;

import org.redisson.api.RedissonClient;
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
@Conditional(ConditionalOnRedissionLock.OnRedissionLockConditional.class)
public @interface ConditionalOnRedissionLock {

    class OnRedissionLockConditional extends AllNestedConditions {

        public OnRedissionLockConditional() {
            super(ConfigurationPhase.PARSE_CONFIGURATION);
        }

        @ConditionalOnClass(RedissonClient.class)
        static class FoundClass {

        }

        @ConditionalOnRedissionLockProperty
        static class FoundProperty {

        }

    }

}
