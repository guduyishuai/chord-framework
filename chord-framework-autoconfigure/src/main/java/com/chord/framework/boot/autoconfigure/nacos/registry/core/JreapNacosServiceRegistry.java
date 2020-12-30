package com.chord.framework.boot.autoconfigure.nacos.registry.core;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.registry.NacosServiceRegistry;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.chord.framework.boot.autoconfigure.nacos.registry.properties.JreapNacosDiscoveryProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.util.StringUtils;

/**
 *
 * 增加了根据组注册的功能
 *
 * 目前官方已支持分组注册
 *
 * @see NacosServiceRegistry
 *
 * Created on 2020/8/21
 *
 * @author: wulinfeng
 */
@Deprecated
public class JreapNacosServiceRegistry extends NacosServiceRegistry {

    private static final Logger log = LoggerFactory.getLogger(NacosServiceRegistry.class);
    private final JreapNacosDiscoveryProperties jreapNacosDiscoveryProperties;
    private final NacosDiscoveryProperties nacosDiscoveryProperties;
    private final NamingService namingService;

    public JreapNacosServiceRegistry(JreapNacosDiscoveryProperties jreapNacosDiscoveryProperties, NacosDiscoveryProperties nacosDiscoveryProperties) {
        super(nacosDiscoveryProperties);
        this.nacosDiscoveryProperties = nacosDiscoveryProperties;
        this.jreapNacosDiscoveryProperties = jreapNacosDiscoveryProperties;
        this.namingService = nacosDiscoveryProperties.namingServiceInstance();
    }

    @Override
    public void register(Registration registration) {
        if (StringUtils.isEmpty(registration.getServiceId())) {
            log.warn("No service to register for nacos client...");
        } else {
            String serviceId = registration.getServiceId();
            Instance instance = new Instance();
            instance.setIp(registration.getHost());
            instance.setPort(registration.getPort());
            instance.setWeight((double)this.nacosDiscoveryProperties.getWeight());
            instance.setClusterName(this.nacosDiscoveryProperties.getClusterName());
            instance.setMetadata(registration.getMetadata());

            try {
                this.namingService.registerInstance(serviceId, this.jreapNacosDiscoveryProperties.getGroup(), instance);
                log.info("nacos registry, {} {}:{} register finished", new Object[]{serviceId, instance.getIp(), instance.getPort()});
            } catch (Exception var5) {
                log.error("nacos registry, {} register failed...{},", new Object[]{serviceId, registration.toString(), var5});
            }

        }
    }

    @Override
    public void deregister(Registration registration) {
        log.info("De-registering from Nacos Server now...");
        if (StringUtils.isEmpty(registration.getServiceId())) {
            log.warn("No dom to de-register for nacos client...");
        } else {
            NamingService namingService = this.nacosDiscoveryProperties.namingServiceInstance();
            String serviceId = registration.getServiceId();

            try {
                namingService.deregisterInstance(serviceId, this.jreapNacosDiscoveryProperties.getGroup(), registration.getHost(), registration.getPort(), this.nacosDiscoveryProperties.getClusterName());
            } catch (Exception var5) {
                log.error("ERR_NACOS_DEREGISTER, de-register failed...{},", registration.toString(), var5);
            }

            log.info("De-registration finished.");
        }
    }

}
