package com.chord.framework.commons.utils;

import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.core.env.Environment;

/**
 * Created on 2020/8/14
 *
 * @author: wulinfeng
 */
public class BindHelper {

    public static <T> void bindProperty(final Environment environment, final String prefix, final T target) {
        ConfigurationPropertySources.attach(environment);
        Binder.get(environment).bind(prefix,
                Bindable.ofInstance(target));
    }

}
