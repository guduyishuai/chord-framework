package com.chord.framework.dubbo.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 *
 * 实现ThreadPool和ThreadLocal适配，保证缓存的生命周期在一个请求之中
 *
 * Created on 2019/9/20
 *
 * @author: wulinfeng
 */
public class RequestScopeCacheHandler implements CacheHandler {

    private static final Logger logger = LoggerFactory.getLogger(RequestScopeCacheHandler.class);

    protected final Lock lock = new ReentrantLock();

    /**
     * 当前线程中的缓存
     */
    protected final ThreadLocal<Map<Object, Object>> store;

    /**
     * 缓存处理过的线程
     */
    protected final Map<Object, Set<String>> cachedThread;

    RequestScopeCacheHandler() {
        this.store = ThreadLocal.withInitial(HashMap::new);
        this.cachedThread = new ConcurrentHashMap<>();
    }

    @Override
    public void put(Object key, Object value) {
        store.get().put(key, value);
        lock.lock();
        try {
            Set<String> threads = cachedThread.get(key);
            if (threads == null || threads.isEmpty()) {
                threads = new HashSet<>();
            }

            final String errorMessage = "获取缓存值错误，请检查dubbo版本";
            try {
                clearCachedThread(key, value, threads);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                logger.error(errorMessage, e);
            }

            logger.debug("clear cache {} {}", key, Thread.currentThread().getName());
            cachedThread.put(key, threads);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Object get(Object key) {
        lock.lock();
        try {
            if (cachedThread.get(key) == null || cachedThread.get(key).stream().noneMatch(Thread.currentThread().getName()::equals)) {
                // 线程池中重复线程强制不走缓存
                Set<String> threadSet = Optional.ofNullable(cachedThread.get(key)).orElseGet(HashSet::new);
                threadSet.add(Thread.currentThread().getName());
                cachedThread.put(key, threadSet);
                logger.debug("no hit cache {} {}", key, Thread.currentThread().getName());
                return null;
            } else {
                // 再次保证不调用线程池重复线程
                logger.debug("hit cache {} {}", key, Thread.currentThread().getName());
                Set<String> otherThread = cachedThread.get(key).stream().filter(o -> !Thread.currentThread().getName().equals(o)).collect(Collectors.toSet());
                cachedThread.get(key).removeAll(otherThread);
            }
        } finally {
          lock.unlock();
        }
        // 返回缓存值
        return store.get().get(key);
    }

    protected void clearCachedThread(Object key, Object value, Set<String> threads) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException{
        Method getter = value.getClass().getMethod("get");
        if (!getter.isAccessible()) {
            getter.setAccessible(true);
        }
        if (getter.invoke(value) == null) {
            // 清除处理过的线程记录，所有线程需要重新建立缓存
            threads.clear();
        }
    }

}
