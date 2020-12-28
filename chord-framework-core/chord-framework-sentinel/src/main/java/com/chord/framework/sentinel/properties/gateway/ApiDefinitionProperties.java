package com.chord.framework.sentinel.properties.gateway;

import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.Set;

/**
 * Created on 2020/5/20
 *
 * @author: wulinfeng
 */
public class ApiDefinitionProperties {

    @NestedConfigurationProperty
    private Set<ApiPredicateItemProperties> item;

    public Set<ApiPredicateItemProperties> getItem() {
        return item;
    }

    public void setItem(Set<ApiPredicateItemProperties> item) {
        this.item = item;
    }
}
