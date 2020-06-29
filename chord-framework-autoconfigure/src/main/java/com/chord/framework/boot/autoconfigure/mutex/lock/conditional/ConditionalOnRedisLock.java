package com.chord.framework.boot.autoconfigure.mutex.lock.conditional;

import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.redis.core.RedisTemplate;

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
@Conditional(ConditionalOnRedisLock.OnRedisLockConditional.class)
public @interface ConditionalOnRedisLock {

    class OnRedisLockConditional extends AllNestedConditions {

        public OnRedisLockConditional() {
            super(ConfigurationPhase.PARSE_CONFIGURATION);
        }

        @ConditionalOnClass(RedisTemplate.class)
        static class FoundClass {

        }

        @ConditionalOnRedisLockProperty
        static class FoundProperty {

        }

    }

}
