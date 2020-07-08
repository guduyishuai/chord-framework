package com.chord.framework.boot.autoconfigure.security.captcha.conditional;

import com.chord.framework.security.SecurityProperties;
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
@Conditional(ConditionalOnCaptchaProperty.OnCaptchaPropertyConditional.class)
public @interface ConditionalOnCaptchaProperty {

    class OnCaptchaPropertyConditional implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            String url = context.getEnvironment().getProperty(SecurityProperties.PREFIX + ".captcha.connection.url");
            String nodes = context.getEnvironment().getProperty(SecurityProperties.PREFIX + ".captcha.connection.cluster.nodes[0]");
            String master = context.getEnvironment().getProperty(SecurityProperties.PREFIX + ".captcha.connection.cluster.master");
            return !StringUtils.isEmpty(url) || !StringUtils.isEmpty(nodes) || !StringUtils.isEmpty(master);
        }

    }

}
