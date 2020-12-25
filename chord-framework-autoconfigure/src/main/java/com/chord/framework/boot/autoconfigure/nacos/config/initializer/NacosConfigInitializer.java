package com.chord.framework.boot.autoconfigure.nacos.config.initializer;

import com.alibaba.cloud.nacos.client.NacosPropertySource;
import com.alibaba.nacos.api.naming.NamingService;
import com.chord.framework.commons.util.BindHelper;
import com.chord.framework.commons.util.OrderComparetor;
import com.chord.framework.nacos.config.handler.ConfigPropertyHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.bootstrap.config.BootstrapPropertySource;
import org.springframework.cloud.bootstrap.config.PropertySourceBootstrapConfiguration;
import org.springframework.cloud.bootstrap.encrypt.EnvironmentDecryptApplicationInitializer;
import org.springframework.cloud.bootstrap.encrypt.KeyProperties;
import org.springframework.cloud.bootstrap.encrypt.RsaProperties;
import org.springframework.cloud.context.encrypt.EncryptorFactory;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Stream;

/**
 * Created on 2020/8/11
 *
 * @author: wulinfeng
 */
@Configuration
public class NacosConfigInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {

    private int order = Ordered.LOWEST_PRECEDENCE;

    private final ServiceLoader<ConfigPropertyHandler> serviceLoader = ServiceLoader.load(ConfigPropertyHandler.class);

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        if("bootstrap".equals(environment.getProperty("spring.config.name"))) {
            return;
        }

        List<ConfigPropertyHandler> propertyHandlerList = new ArrayList<>();
        Iterator<ConfigPropertyHandler> iterator = serviceLoader.iterator();
        if(iterator.hasNext()) {
            propertyHandlerList.add(iterator.next());
        }
        propertyHandlerList = OrderComparetor.compare(propertyHandlerList);

        if(propertyHandlerList.isEmpty()) {
            return;
        }

        Set<String> changed = new HashSet<>();

        KeyProperties keyProperties = new KeyProperties();
        BindHelper.bindProperty(environment, "encrypt", keyProperties);

        RsaProperties rsaProperties = new RsaProperties();
        BindHelper.bindProperty(environment, "encrypt.rsa", rsaProperties);

        MutablePropertySources propertySources = environment.getPropertySources();
        List<ConfigPropertyHandler> finalPropertyHandlerList = propertyHandlerList;
        propertySources.stream()
            .filter(propertySource -> propertySource.getName().startsWith(PropertySourceBootstrapConfiguration.BOOTSTRAP_PROPERTY_SOURCE_NAME))
            .forEach(propertySource -> {
                String[] propertyNames = new String[0];
                if(propertySource instanceof  NacosPropertySource) {
                    propertyNames = ((NacosPropertySource) propertySource).getPropertyNames();
                }
                else if(propertySource instanceof BootstrapPropertySource) {
                    propertyNames = ((BootstrapPropertySource) propertySource).getPropertyNames();
                }
                Stream.of(propertyNames)
                        .forEach(key -> {
                            final String value = propertySource.getProperty(key).toString();
                            String plainText = value;
                            if(!StringUtils.isEmpty(value) && value.startsWith(EnvironmentDecryptApplicationInitializer.ENCRYPTED_PROPERTY_PREFIX)) {
                                // 解密
                                String cipherText = value.replace(EnvironmentDecryptApplicationInitializer.ENCRYPTED_PROPERTY_PREFIX, "");

                                TextEncryptor textEncryptor =
                                        new EncryptorFactory(keyProperties.getSalt()).create(keyProperties.getKey());

                                plainText = textEncryptor.decrypt(cipherText);
                            }

                            // 对配置信息进行处理
                            String preText = value;
                            for(ConfigPropertyHandler propertyHandler : finalPropertyHandlerList) {
                                preText = propertyHandler.handle(key, plainText, value, preText);
                            }

                            if(!value.equals(preText)) {
                                changed.add(key);
                                // 重新设置值
                                if(propertySource instanceof  NacosPropertySource) {
                                    Map source = ((NacosPropertySource) propertySource).getSource();
                                    source.put(key, value);
                                }else if(propertySource instanceof BootstrapPropertySource) {
                                    Map source = (Map) ((BootstrapPropertySource) propertySource).getSource();
                                    source.put(key, value);
                                }
                            }

                        });
            });

        if(!changed.isEmpty()) {
            ApplicationContext parent = applicationContext.getParent();
            if (parent != null) {
                parent.publishEvent(new EnvironmentChangeEvent(parent, changed));
            }
        }

    }

    @Override
    public int getOrder() {
        return this.order;
    }

}
