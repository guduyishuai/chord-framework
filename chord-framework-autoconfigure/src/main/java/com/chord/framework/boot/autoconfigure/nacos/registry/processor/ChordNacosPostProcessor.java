package com.chord.framework.boot.autoconfigure.nacos.registry.processor;

import com.alibaba.cloud.nacos.discovery.NacosServiceDiscovery;
import com.chord.framework.boot.autoconfigure.nacos.registry.annotation.EnableDiscoveryWithNacosGroup;
import com.chord.framework.nacos.registry.initializer.ChordNacosApplicationContextInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 *
 * 根据{@link EnableDiscoveryWithNacosGroup}判断是否需要排除NacosDiscoveryAutoConfiguration
 *
 * @see ChordNacosApplicationContextInitializer
 *
 * 官方已经支持分组注册
 *
 * @see NacosServiceDiscovery
 *
 * Created on 2020/9/4
 *
 * @author: wulinfeng
 */
@Deprecated
public class ChordNacosPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        EnableDiscoveryWithNacosGroup enableDiscoveryWithNacosGroup =
                AnnotationUtils.findAnnotation(application.getMainApplicationClass(), EnableDiscoveryWithNacosGroup.class);

        if(enableDiscoveryWithNacosGroup != null) {
            application.addInitializers(new ChordNacosApplicationContextInitializer());
        }
    }

}
