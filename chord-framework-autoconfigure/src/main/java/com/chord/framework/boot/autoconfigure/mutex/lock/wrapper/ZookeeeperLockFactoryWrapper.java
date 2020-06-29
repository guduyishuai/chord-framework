package com.chord.framework.boot.autoconfigure.mutex.lock.wrapper;

import com.chord.framework.mutex.lock.Lock;
import com.chord.framework.mutex.lock.LockFactory;
import com.chord.framework.mutex.lock.zookeeper.ZookeeperLockFactory;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.Closeable;

/**
 * Created on 2020/6/29
 *
 * @author: wulinfeng
 */
public class ZookeeeperLockFactoryWrapper implements LockFactory, ApplicationContextAware, Closeable {

    private ApplicationContext applicationContext;

    private ThreadLocal<ZookeeperLockFactory> holder = new ThreadLocal<>();

    @Override
    public Lock create(String name) {
        if(holder.get() == null) {
            createLockFactory();
        }
        return holder.get().create(name);
    }

    public CuratorFramework getCuratorFramework() {
        if(holder.get() == null) {
            createLockFactory();
        }
        return holder.get().getCuratorFramework();
    }

    private void createLockFactory() {
        holder.set(applicationContext.getBean(ZookeeperLockFactory.class));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void close() {
        holder.remove();
    }
}
