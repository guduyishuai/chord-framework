package com.chord.framework.mybatis.runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static com.baomidou.mybatisplus.core.toolkit.StringPool.DOT;

/**
 * Created on 2020/6/9
 *
 * @author: wulinfeng
 */
public class StrictMap<V> extends HashMap<String, V> {

    private static Logger logger = LoggerFactory.getLogger(StrictMap.class);

    private String name;

    public StrictMap(String name, int initialCapacity, float loadFactor)
    {
        super(initialCapacity, loadFactor);
        this.name = name;
    }

    public StrictMap(String name, int initialCapacity)
    {
        super(initialCapacity);
        this.name = name;
    }

    public StrictMap(String name)
    {
        super();
        this.name = name;
    }

    public StrictMap(String name, Map<String, ? extends V> m)
    {
        super(m);
        this.name = name;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V put(String key, V value)
    {
        if (key.contains(DOT))
        {
            final String shortKey = getShortName(key);
            if (super.get(shortKey) == null)
            {
                super.put(shortKey, value);
            } else
            {
                super.put(shortKey, (V) new Ambiguity(shortKey));
            }
        }
        logger.debug("生成mappedStatement:" + value);
        return super.put(key, value);
    }

    @Override
    public V get(Object key)
    {
        V value = super.get(key);
        if (value == null)
        {
            throw new IllegalArgumentException(name + " does not contain value for " + key);
        }
        if (value instanceof Ambiguity)
        {
            throw new IllegalArgumentException(((Ambiguity) value).getSubject() + " is ambiguous in " + name
                    + " (try using the full name including the namespace, or rename one of the entries)");
        }
        return value;
    }

    private String getShortName(String key)
    {
        final String[] keyparts = key.split("\\.");
        return keyparts[keyparts.length - 1];
    }

    protected static class Ambiguity
    {
        private String subject;

        public Ambiguity(String subject)
        {
            this.subject = subject;
        }

        public String getSubject()
        {
            return subject;
        }
    }

}
