package com.chord.framework.dubbo.registry;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.chord.framework.dubbo.commons.ExtPropertyKeyConst;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.registry.Registry;
import org.apache.dubbo.registry.nacos.NacosRegistryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static com.alibaba.nacos.api.PropertyKeyConst.*;
import static com.alibaba.nacos.client.naming.utils.UtilAndComs.NACOS_NAMING_LOG_NAME;
import static org.apache.dubbo.common.constants.RemotingConstants.BACKUP_KEY;

/**
 * Created on 2020/8/21
 *
 * @author: wulinfeng
 */
public class ChordNacosRegistryFactory extends NacosRegistryFactory {

    private static final Logger logger = LoggerFactory.getLogger(ChordNacosRegistryFactory.class);

    @Override
    protected Registry createRegistry(URL url) {
        url = url.setProtocol(ExtPropertyKeyConst.PROTOCOL_NACOS);
        Properties nacosProperties = resolveProperties(url);
        return new ChordNacosRegistry(url, buildNamingService(nacosProperties), buildGroupName(nacosProperties), buildClusterName(nacosProperties));
    }

    private Properties resolveProperties(URL url) {
        return buildNacosProperties(url);
    }

    private NamingService buildNamingService(Properties nacosProperties) {
        NamingService namingService;
        try {
            namingService = NacosFactory.createNamingService(nacosProperties);
        } catch (NacosException e) {
            if (logger.isErrorEnabled()) {
                logger.error(e.getErrMsg(), e);
            }
            throw new IllegalStateException(e);
        }
        return namingService;
    }

    private String buildGroupName(Properties nacosProperties) {
        return nacosProperties.getProperty(ExtPropertyKeyConst.GROUP, Constants.DEFAULT_GROUP);
    }

    private Properties buildNacosProperties(URL url) {
        Properties properties = new Properties();
        setServerAddr(url, properties);
        setProperties(url, properties);
        return properties;
    }

    private String buildClusterName(Properties nacosProperties) {
        return nacosProperties.getProperty(ExtPropertyKeyConst.CLUSTER_NAME, Constants.DEFAULT_CLUSTER_NAME);
    }

    private void setServerAddr(URL url, Properties properties) {
        StringBuilder serverAddrBuilder =
                new StringBuilder(url.getHost()) // Host
                        .append(":")
                        .append(url.getPort()); // Port

        // Append backup parameter as other servers
        String backup = url.getParameter(BACKUP_KEY);
        if (backup != null) {
            serverAddrBuilder.append(",").append(backup);
        }

        String serverAddr = serverAddrBuilder.toString();
        properties.put(SERVER_ADDR, serverAddr);
    }

    private void setProperties(URL url, Properties properties) {
        putPropertyIfAbsent(url, properties, NAMESPACE);
        putPropertyIfAbsent(url, properties, ExtPropertyKeyConst.GROUP);
        putPropertyIfAbsent(url, properties, NACOS_NAMING_LOG_NAME);
        putPropertyIfAbsent(url, properties, ENDPOINT);
        putPropertyIfAbsent(url, properties, ACCESS_KEY);
        putPropertyIfAbsent(url, properties, SECRET_KEY);
        putPropertyIfAbsent(url, properties, CLUSTER_NAME);
    }

    private void putPropertyIfAbsent(URL url, Properties properties, String propertyName) {
        String propertyValue = url.getParameter(propertyName);
        if (propertyValue != null && propertyValue.trim().length() != 0) {
            properties.setProperty(propertyName, propertyValue);
        }
    }

}
