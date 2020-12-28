package com.chord.framework.commons.initializer;

import com.chord.framework.commons.utils.OrderUtils;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;

import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created on 2020/12/25
 *
 * @author: wulinfeng
 */
public class ChordApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {

    private int order = Ordered.LOWEST_PRECEDENCE;

    private final ServiceLoader<ChordInitializer> serviceLoader = ServiceLoader.load(ChordInitializer.class);

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        Set<ChordInitializer> initializerSet = new TreeSet<>();
        serviceLoader.forEach(chordInitializer -> {
            initializerSet.add(chordInitializer);
        });
        initializerSet.stream().sorted((o1, o2) ->
            OrderUtils.resolveOrder(o2) - OrderUtils.resolveOrder(o1)
        ).forEach(chordInitializer -> chordInitializer.initialize(applicationContext));
    }

    @Override
    public int getOrder() {
        return this.order;
    }

}
