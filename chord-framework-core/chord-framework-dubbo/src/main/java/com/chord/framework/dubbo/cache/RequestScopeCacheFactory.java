package com.chord.framework.dubbo.cache;

import org.apache.dubbo.cache.Cache;
import org.apache.dubbo.cache.support.AbstractCacheFactory;
import org.apache.dubbo.common.URL;

/**
 *
 * 创建{@link RequestScopeCache}的工厂
 * {@see RequestScopeCache}
 *
 * Created on 2019/9/19
 *
 * @author: wulinfeng
 */
public class RequestScopeCacheFactory extends AbstractCacheFactory {

    @Override
    protected Cache createCache(URL url) {
        return new RequestScopeCache(url);
    }

}
