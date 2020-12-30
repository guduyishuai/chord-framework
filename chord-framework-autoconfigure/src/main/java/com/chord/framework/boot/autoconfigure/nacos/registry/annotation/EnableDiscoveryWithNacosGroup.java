package com.chord.framework.boot.autoconfigure.nacos.registry.annotation;

import com.alibaba.cloud.nacos.registry.NacosServiceRegistry;
import com.chord.framework.boot.autoconfigure.nacos.registry.JreapNacosDiscoveryAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 *
 * 用于分组注册
 * 目前官方已经支持分组注册
 *
 * @see NacosServiceRegistry
 *
 * Created on 2020/8/21
 *
 * @author: wulinfeng
 */
@Deprecated
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(JreapNacosDiscoveryAutoConfiguration.class)
public @interface EnableDiscoveryWithNacosGroup {

}
