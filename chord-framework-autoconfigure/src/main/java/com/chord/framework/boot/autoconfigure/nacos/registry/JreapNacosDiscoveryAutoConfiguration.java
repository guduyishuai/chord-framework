package com.chord.framework.boot.autoconfigure.nacos.registry;

import com.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.registry.NacosAutoServiceRegistration;
import com.alibaba.cloud.nacos.registry.NacosRegistration;
import com.alibaba.cloud.nacos.registry.NacosServiceRegistry;
import com.chord.framework.boot.autoconfigure.nacos.registry.core.JreapNacosServiceRegistry;
import com.chord.framework.boot.autoconfigure.nacos.registry.properties.JreapNacosDiscoveryProperties;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationAutoConfiguration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Created on 2020/8/21
 *
 * @author: wulinfeng
 */
@Configuration
@EnableConfigurationProperties({JreapNacosDiscoveryProperties.class, NacosDiscoveryProperties.class})
@ConditionalOnNacosDiscoveryEnabled
@ConditionalOnProperty(
        value = {"spring.cloud.service-registry.auto-registration.enabled"},
        matchIfMissing = true
)
@AutoConfigureAfter({AutoServiceRegistrationConfiguration.class, AutoServiceRegistrationAutoConfiguration.class})
public class JreapNacosDiscoveryAutoConfiguration {

    @Primary
    @Bean
    public NacosServiceRegistry nacosServiceRegistry(JreapNacosDiscoveryProperties jreapNacosDiscoveryProperties, NacosDiscoveryProperties nacosDiscoveryProperties) {
        return new JreapNacosServiceRegistry(jreapNacosDiscoveryProperties, nacosDiscoveryProperties);
    }

    @Bean
    public NacosRegistration nacosRegistration(NacosDiscoveryProperties nacosDiscoveryProperties, ApplicationContext context) {
        return new NacosRegistration(nacosDiscoveryProperties, context);
    }

    @Bean
    public NacosAutoServiceRegistration nacosAutoServiceRegistration(NacosServiceRegistry registry, AutoServiceRegistrationProperties autoServiceRegistrationProperties, NacosRegistration registration) {
        return new NacosAutoServiceRegistration(registry, autoServiceRegistrationProperties, registration);
    }

}
