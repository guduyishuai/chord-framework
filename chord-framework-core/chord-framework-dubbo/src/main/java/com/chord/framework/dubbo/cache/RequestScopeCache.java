package com.chord.framework.dubbo.cache;

import org.apache.dubbo.cache.Cache;
import org.apache.dubbo.common.URL;

/**
 *
 * 扩展dubbo缓存，实现ThreadPool和ThreadLocal适配，保证缓存的生命周期在一个请求之中
 *
 * Created on 2019/9/19
 *
 * @author: wulinfeng
 */
public class RequestScopeCache implements Cache {

    /**
     * 缓存处理器
     */
    private final CacheHandler requestScopeCacheHandler;

    RequestScopeCache(URL url) {
        this.requestScopeCacheHandler = new RequestScopeCacheHandler();
    }

    /**
     *
     * 放入缓存
     *
     * @param key
     * @param value
     */
    @Override
    public void put(Object key, Object value) {
        requestScopeCacheHandler.put(key, value);
    }

    /**
     *
     * 获取缓存
     *
     * @param key
     * @return
     */
    @Override
    public Object get(Object key) {
        return requestScopeCacheHandler.get(key);
    }

}
