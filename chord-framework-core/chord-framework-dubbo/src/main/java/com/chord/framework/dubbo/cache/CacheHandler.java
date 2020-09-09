package com.chord.framework.dubbo.cache;

/**
 *
 * 缓存处理器
 *
 * Created on 2019/10/10
 *
 * @author: wulinfeng
 */
public interface CacheHandler {

    /**
     *
     *  设置缓存
     *
     * @param key
     * @param value
     */
    void put(Object key, Object value);

    /**
     *
     *  获得缓存
     *
     * @param key
     * @return
     */
    Object get(Object key);

}
