package com.chord.framework.commons.util;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created on 2020/8/17
 *
 * @author: wulinfeng
 */
public class OrderComparetor {

    public static <T> List<T> compare(Collection<T> object) {
        return object.stream().sorted((o1, o2) -> resolveOrder(o2.getClass()) - resolveOrder(o1.getClass())).collect(Collectors.toList());
    }

    private static <T> int resolveOrder(Class<T> object) {
        Order ann = AnnotatedElementUtils.findMergedAnnotation(object.getClass(), Order.class);
        return ann != null ? ann.value() : 0;
    }

}
