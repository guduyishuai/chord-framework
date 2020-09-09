package com.chord.framework.boot.autoconfigure.nacos.registry.processor;

import com.chord.framework.boot.autoconfigure.nacos.registry.annotation.EnableDiscoveryWithNacosGroup;
import com.chord.framework.boot.autoconfigure.nacos.registry.initializer.JreapNacosApplicationContextInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 *
 * 根据{@link EnableDiscoveryWithNacosGroup}判断是否需要排除NacosDiscoveryAutoConfiguration
 *
 * @see JreapNacosApplicationContextInitializer
 *
 * Created on 2020/9/4
 *
 * @author: wulinfeng
 */
public class JreapNacosPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        EnableDiscoveryWithNacosGroup enableDiscoveryWithNacosGroup =
                AnnotationUtils.findAnnotation(application.getMainApplicationClass(), EnableDiscoveryWithNacosGroup.class);

        if(enableDiscoveryWithNacosGroup != null) {
            application.addInitializers(new JreapNacosApplicationContextInitializer());
        }
    }

}
