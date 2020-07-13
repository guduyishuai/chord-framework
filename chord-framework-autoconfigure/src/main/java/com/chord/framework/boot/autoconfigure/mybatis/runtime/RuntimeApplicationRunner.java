package com.chord.framework.boot.autoconfigure.mybatis.runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 *
 * 替换Mybatis的Configuration中的不可修改的Map，并且替换为线程安全的Map，因为Configuration本身非线程安全
 *
 * Created on 2020/6/8
 *
 * @author: wulinfeng
 */
public class RuntimeApplicationRunner implements ApplicationRunner, ApplicationContextAware {

    public static final Logger logger = LoggerFactory.getLogger(RuntimeApplicationRunner.class);

    private ApplicationContext applicationContext;

    @Override
    public void run(ApplicationArguments args) throws Exception {

//        SqlSessionFactory sqlSessionFactory = applicationContext.getBean(SqlSessionFactory.class);
//        Configuration configuration = sqlSessionFactory.getConfiguration();
//        for (MybatisConfigurationUnmodifyMapField unmodifyMapField : MybatisConfigurationUnmodifyMapField.values())
//        {
//            replaceCollectionsToSynchornizeModifyInMybaits(configuration, unmodifyMapField);
//        }

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
//
//    /**
//     *
//     * 替换Configuration中的集合为可修改的，线程安全的集合
//     *
//     * @param configuration
//     * @param unmodifyMapField
//     * @throws IllegalAccessException
//     */
//    public void replaceCollectionsToSynchornizeModifyInMybaits(Configuration configuration, MybatisConfigurationUnmodifyMapField unmodifyMapField) {
//
//        Field field = null;
//        Map map = null;
//        try {
//            field = ReflectionUtils.findField(configuration.getClass(), unmodifyMapField.getName());
//            field.setAccessible(true);
//            map = (Map) Optional.ofNullable(field.get(configuration)).orElseThrow(NullPointerException::new);
//        } catch (IllegalAccessException e) {
//            String errorMessage = String.format("mybatis热加载初始化失败！很可能是mybatis的版本问题，请检查Configuration中是否有成员变量%s", unmodifyMapField.getName());
//            logger.error(errorMessage, e);
//            throw new MybatisRuntimeInitException(errorMessage);
//        } catch (NullPointerException e) {
//            String errorMessage = String.format("mybatis热加载初始化失败！很可能是mybatis的版本问题，Configuration所需成员变量为null");
//            logger.error(errorMessage, e);
//            throw new MybatisRuntimeInitException(errorMessage);
//        }
//
//        Map synchronizedModifyAbleMap =
//            Collections.synchronizedMap(new StrictMap(unmodifyMapField.getCollectionName()));
//        for (Object key : map.keySet())
//        {
//            try
//            {
//                synchronizedModifyAbleMap.put(key, map.get(key));
//            } catch (IllegalArgumentException e)
//            {
//                String warnMessage = "mybatis热加载初始化put警告！原因有value是null，或者key重复，如果重复，value将封装成Ambiguity。详细信息{}";
//                logger.warn(warnMessage, e.getMessage());
//            }
//        }
//        try {
//            field.set(configuration, synchronizedModifyAbleMap);
//        } catch (IllegalAccessException e) {
//            String errorMessage = "mybatis热加载初始化失败！";
//            logger.error(errorMessage, e);
//            throw new MybatisRuntimeInitException(errorMessage);
//        }
//    }
//
//    private enum MybatisConfigurationUnmodifyMapField {
//
//        MAPPED_STATEMENTS("mappedStatements", "Mapped Statements collection"),
//        CACHES("caches", "Caches collection"),
//        RESULT_MAPS("resultMaps", "Result Maps collection"),
//        PARAMETER_MAPS("parameterMaps", "Parameter Maps collection"),
//        KEY_GENERATORS("keyGenerators", "Key Generators collection"),
//        SQL_FRAGMENTS("sqlFragments", "XML fragments parsed from previous mappers");
//
//        private String name;
//        private String collectionName;
//
//        MybatisConfigurationUnmodifyMapField(String name, String collectionName) {
//            this.name = name;
//            this.collectionName = collectionName;
//        }
//
//        public String getName() {
//            return name;
//        }
//
//        public String getCollectionName() {
//            return collectionName;
//        }
//    }
//
//    private enum MybatisConfigurationResourceField {
//
//        LOADED_RESOURCES("loadedResources");
//
//        private String name;
//
//        MybatisConfigurationResourceField(String name) {
//            this.name = name;
//        }
//
//        public String getName() {
//            return name;
//        }
//
//    }
//
//    private enum MybatisConfigurationIncompleteField {
//
//        INCOMPLETE_STATEMENTS("incompleteStatements"),
//        INCOMPLETE_CACHE_REFS("incompleteCacheRefs"),
//        INCOMPLETE_RESULT_MAPS("incompleteResultMaps"),
//        INCOMPLETE_METHODS("incompleteMethods");
//
//        private String name;
//
//        MybatisConfigurationIncompleteField(String name) {
//            this.name = name;
//        }
//
//        public String getName() {
//            return name;
//        }

//    }

}
