package com.chord.framework.commons.initializer;

import org.springframework.context.ApplicationContext;

/**
 * Created on 2020/12/25
 *
 * @author: wulinfeng
 */
public interface ChordInitializer {

    void initialize(ApplicationContext applicationContext);

}
