package com.chord.framework.boot.autoconfigure.nacos.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.bootstrap.encrypt.KeyProperties;
import org.springframework.cloud.bootstrap.encrypt.RsaProperties;
import org.springframework.cloud.context.encrypt.EncryptorFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.util.StringUtils;

/**
 *
 * 加密配置
 *
 * Created on 2020/8/13
 *
 * @author: wulinfeng
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(BytesEncryptor.class)
@EnableConfigurationProperties({KeyProperties.class, RsaProperties.class})
public class EncryptionAutoConfiguration {

    @Autowired
    private KeyProperties key;

    @Bean
    @ConditionalOnMissingBean
    public TextEncryptor defaultTextEncryptor() {
        if (StringUtils.hasText(this.key.getKey())) {
            return new EncryptorFactory(this.key.getSalt()).create(this.key.getKey());
        }
        return Encryptors.noOpText();
    }

}
