package com.chord.framework.commons.utils;

import org.springframework.core.Ordered;

/**
 * Created on 2020/12/25
 *
 * @author: wulinfeng
 */
public class OrderUtils {

    public static int resolveOrder(Object o) {
        if(Ordered.class.isAssignableFrom(o.getClass())) {
            return ((Ordered) o).getOrder();
        }
        return org.springframework.core.annotation.OrderUtils.getOrder(o.getClass());
    }

}
