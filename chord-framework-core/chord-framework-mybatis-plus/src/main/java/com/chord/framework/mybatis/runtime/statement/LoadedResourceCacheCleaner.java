package com.chord.framework.mybatis.runtime.statement;

import com.chord.framework.mybatis.runtime.CacheCleaner;
import com.chord.framework.mybatis.runtime.exception.MybatisRuntimeCleanException;
import com.chord.framework.mybatis.runtime.utils.ReflectionUtils;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Set;

/**
 * Created on 2020/6/8
 *
 * @author: wulinfeng
 */
public class LoadedResourceCacheCleaner implements CacheCleaner {

    private final static Logger logger = LoggerFactory.getLogger(LoadedResourceCacheCleaner.class);

    private Configuration configuration;

    private String resource;

    public LoadedResourceCacheCleaner(Configuration configuration, String resource) {
        this.configuration = configuration;
        this.resource = resource;
    }

    @Override
    public boolean clean() {
        Field loadedResourcesField = ReflectionUtils.findField(configuration.getClass(), "loadedResources");
        loadedResourcesField.setAccessible(true);
        Set loadedResourcesSet = null;
        try {
            loadedResourcesSet = ((Set) loadedResourcesField.get(configuration));
        } catch (IllegalAccessException e) {
            String errorMessage = "清除Configuration出错，检查mybatis版本";
            logger.error(errorMessage, e);
            throw new MybatisRuntimeCleanException(errorMessage);
        }
        loadedResourcesSet.remove(resource);
        return true;
    }

}
