package com.chord.framework.boot.autoconfigure.nacos.registry.annotation;

import com.chord.framework.boot.autoconfigure.nacos.registry.JreapNacosDiscoveryAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Created on 2020/8/21
 *
 * @author: wulinfeng
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(JreapNacosDiscoveryAutoConfiguration.class)
public @interface EnableDiscoveryWithNacosGroup {

}
