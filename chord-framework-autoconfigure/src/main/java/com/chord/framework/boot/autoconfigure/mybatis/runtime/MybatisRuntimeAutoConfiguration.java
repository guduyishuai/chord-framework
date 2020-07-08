package com.chord.framework.boot.autoconfigure.mybatis.runtime;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created on 2020/6/10
 *
 * @author: wulinfeng
 */
@Configuration
public class MybatisRuntimeAutoConfiguration {

    @Bean
    public RuntimeApplicationRunner runtimeApplicationRunner() {
        return new RuntimeApplicationRunner();
    }

}
