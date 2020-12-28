package com.chord.framework.commons.listener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.ServiceLoader;

/**
 * Created on 2020/12/29
 *
 * @author: wulinfeng
 */
public class ChordApplicationRunListener implements SpringApplicationRunListener {

    private final ServiceLoader<ApplicationRunListener> serviceLoader = ServiceLoader.load(ApplicationRunListener.class);

    private final SpringApplication application;

    private final String[] args;

    public ChordApplicationRunListener(SpringApplication application, String[] args){
        this.application = application;
        this.args = args;
    }

    public void starting() {
        serviceLoader.forEach(ApplicationRunListener::starting);
    }

    public void environmentPrepared(ConfigurableEnvironment environment) {
        serviceLoader.forEach(applicationRunListener -> applicationRunListener.environmentPrepared(environment));
    }

    public void contextPrepared(ConfigurableApplicationContext context) {
        serviceLoader.forEach(applicationRunListener -> applicationRunListener.contextPrepared(context));
    }

    public void contextLoaded(ConfigurableApplicationContext context) {
        serviceLoader.forEach(applicationRunListener -> applicationRunListener.contextLoaded(context));
    }

    public void started(ConfigurableApplicationContext context) {
        serviceLoader.forEach(applicationRunListener -> applicationRunListener.started(context));
    }

    public void running(ConfigurableApplicationContext context) {
        serviceLoader.forEach(applicationRunListener -> applicationRunListener.running(context));
    }

    public void failed(ConfigurableApplicationContext context, Throwable exception) {
        serviceLoader.forEach(applicationRunListener -> applicationRunListener.failed(context, exception));
    }

}
