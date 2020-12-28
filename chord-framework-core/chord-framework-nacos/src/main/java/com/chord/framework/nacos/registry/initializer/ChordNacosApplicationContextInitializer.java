package com.chord.framework.nacos.registry.initializer;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import java.util.Map;

/**
 *
 * 排除NacosDiscoveryAutoConfiguration
 *
 * Created on 2020/9/4
 *
 * @author: wulinfeng
 */
public class ChordNacosApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {

    private static final String PROPERTY_SOURCE_DEFAULT_BOOTSTRAP = "applicationConfig: [classpath:/bootstrap.yml]";

    private static final String PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE = "spring.autoconfigure.exclude";

    private static final String NACOS_DISCOVERY_AUTO_CONFIGURATION = "org.springframework.cloud.alibaba.nacos.NacosDiscoveryAutoConfiguration";

    private static final String SPLITER = ",";

    private int order = Ordered.LOWEST_PRECEDENCE;

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        Environment environment = applicationContext.getEnvironment();
        ((ConfigurableEnvironment) environment).getPropertySources().forEach(propertySource -> {
            if(PROPERTY_SOURCE_DEFAULT_BOOTSTRAP.equals(propertySource.getName())) {
                String exclude;
                if(propertySource.containsProperty(PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE)) {
                    exclude = propertySource.getProperty(PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE) + SPLITER + NACOS_DISCOVERY_AUTO_CONFIGURATION;
                } else {
                    exclude = NACOS_DISCOVERY_AUTO_CONFIGURATION;
                }

                ((Map<String, Object>) propertySource.getSource()).put(PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE, exclude);
            }
        });
    }

    @Override
    public int getOrder() {
        return this.order;
    }

}
