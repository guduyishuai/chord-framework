package com.chord.framework.commons.utils;

import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.core.env.Environment;

/**
 * Created on 2019/10/17
 *
 * @author: wulinfeng
 */
public class ContextUtils {

    public static boolean isBootstap(final Environment environment) {
        return "bootstrap".equals(environment.getProperty("spring.config.name"));
    }

    public static <T> void bindProperty(final Environment environment, final String prefix, final T target) {
        ConfigurationPropertySources.attach(environment);
        Binder.get(environment).bind(prefix,
                Bindable.ofInstance(target));
    }

}
