package com.chord.framework.boot.autoconfigure.nacos.config.annotation;

import com.chord.framework.boot.autoconfigure.nacos.config.EncryptionConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Created on 2020/8/13
 *
 * @author: wulinfeng
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(EncryptionConfiguration.class)
public @interface EnableNacosEncryptionRestApi {
}
