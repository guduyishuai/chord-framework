package com.chord.framework.boot.autoconfigure.nacos.config;

import com.chord.framework.boot.autoconfigure.nacos.config.properties.NacosConfigProperties;
import com.chord.framework.nacos.config.encryption.EncryptionController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.TextEncryptor;

/**
 * Created on 2020/8/13
 *
 * @author: wulinfeng
 */
@EnableConfigurationProperties(NacosConfigProperties.class)
@Configuration(proxyBeanMethods = false)
public class EncryptionConfiguration {

    @Autowired(required = false)
    private TextEncryptor encryptor;

    @Autowired
    private NacosConfigProperties properties;

    @Bean
    public EncryptionController encryptionController() {
        EncryptionController controller = new EncryptionController(this.encryptor);
        controller.setDefaultApplicationName(this.properties.getDefaultApplicationName());
        controller.setDefaultProfile(this.properties.getDefaultProfile());
        return controller;
    }

}
