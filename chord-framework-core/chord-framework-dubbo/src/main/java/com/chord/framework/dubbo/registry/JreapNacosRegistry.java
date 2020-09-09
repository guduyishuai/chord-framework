package com.chord.framework.dubbo.registry;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.utils.UrlUtils;
import org.apache.dubbo.registry.NotifyListener;
import org.apache.dubbo.registry.nacos.NacosRegistry;
import org.apache.dubbo.registry.nacos.NacosServiceName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static java.util.Collections.singleton;
import static org.apache.dubbo.common.constants.CommonConstants.*;
import static org.apache.dubbo.common.constants.RegistryConstants.*;
import static org.apache.dubbo.registry.Constants.ADMIN_PROTOCOL;
import static org.apache.dubbo.registry.nacos.NacosServiceName.valueOf;

/**
 * Created on 2020/8/21
 *
 * @author: wulinfeng
 */
public class JreapNacosRegistry extends NacosRegistry {

    private static final Logger logger = LoggerFactory.getLogger(JreapNacosRegistry.class);

    private static final String SERVICE_NAME_SEPARATOR = System.getProperty("nacos.service.name.separator", ":");

    private static final long LOOKUP_INTERVAL = Long.getLong("nacos.service.names.lookup.interval", 30);

    private static final int PAGINATION_SIZE = Integer.getInteger("nacos.service.names.pagination.size", 100);

    private final String groupName;

    private final NamingService namingService;

    private final ConcurrentMap<String, com.alibaba.nacos.api.naming.listener.EventListener> nacosListeners;

    private volatile ScheduledExecutorService scheduledExecutorService;

    private static final int CATEGORY_INDEX = 0;

    private static final int SERVICE_INTERFACE_INDEX = 1;

    private static final int SERVICE_VERSION_INDEX = 2;

    private static final int SERVICE_GROUP_INDEX = 3;

    private static final String WILDCARD = "*";

    private static final String[] ALL_SUPPORTED_CATEGORIES = of(
            PROVIDERS_CATEGORY,
            CONSUMERS_CATEGORY,
            ROUTERS_CATEGORY,
            CONFIGURATORS_CATEGORY
    );

    public JreapNacosRegistry(URL url, NamingService namingService, String groupName) {
        super(url, namingService);
        this.namingService = namingService;
        this.nacosListeners = new ConcurrentHashMap<>();
        this.groupName = groupName;
    }

    @Override
    public List<URL> lookup(final URL url) {
        final List<URL> urls = new LinkedList<>();
        execute(namingService -> {
            Set<String> serviceNames = getServiceNames(url, null);
            for (String serviceName : serviceNames) {
                List<Instance> instances = namingService.getAllInstances(serviceName, groupName);
                urls.addAll(buildURLs(url, instances));
            }
        });
        return urls;
    }

    @Override
    public void doRegister(URL url) {
        final String serviceName = getServiceName(url);
        final Instance instance = createInstance(url);
        execute(namingService -> namingService.registerInstance(serviceName, groupName, instance));
    }

    @Override
    public void doUnregister(final URL url) {
        execute(namingService -> {
            String serviceName = getServiceName(url);
            Instance instance = createInstance(url);
            namingService.deregisterInstance(serviceName, groupName, instance.getIp(), instance.getPort());
        });
    }

    @Override
    public void doSubscribe(final URL url, final NotifyListener listener) {
        Set<String> serviceNames = getServiceNames(url, listener);
        doSubscribe(url, listener, serviceNames);
    }

    private void doSubscribe(final URL url, final NotifyListener listener, final Set<String> serviceNames) {
        execute(namingService -> {
            for (String serviceName : serviceNames) {
                List<Instance> instances = namingService.getAllInstances(serviceName, groupName);
                notifySubscriber(url, listener, instances);
                subscribeEventListener(serviceName, url, listener);
            }
        });
    }

    private void notifySubscriber(URL url, NotifyListener listener, Collection<Instance> instances) {
        List<Instance> healthyInstances = new LinkedList<>(instances);
        // Healthy Instances
        filterHealthyInstances(healthyInstances);
        List<URL> urls = buildURLs(url, healthyInstances);
        this.notify(url, listener, urls);
    }

    private void subscribeEventListener(String serviceName, final URL url, final NotifyListener listener)
            throws NacosException {
        if (!nacosListeners.containsKey(serviceName)) {
            com.alibaba.nacos.api.naming.listener.EventListener eventListener = event -> {
                if (event instanceof NamingEvent) {
                    NamingEvent e = (NamingEvent) event;
                    notifySubscriber(url, listener, e.getInstances());
                }
            };
            namingService.subscribe(serviceName, groupName, eventListener);
            nacosListeners.put(serviceName, eventListener);
        }
    }

    private void scheduleServiceNamesLookup(final URL url, final NotifyListener listener) {
        if (scheduledExecutorService == null) {
            scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutorService.scheduleAtFixedRate(() -> {
                Set<String> serviceNames = getAllServiceNames();
                filterData(serviceNames, serviceName -> {
                    boolean accepted = false;
                    for (String category : ALL_SUPPORTED_CATEGORIES) {
                        String prefix = category + SERVICE_NAME_SEPARATOR;
                        if (StringUtils.startsWith(serviceName, prefix)) {
                            accepted = true;
                            break;
                        }
                    }
                    return accepted;
                });
                doSubscribe(url, listener, serviceNames);
            }, LOOKUP_INTERVAL, LOOKUP_INTERVAL, TimeUnit.SECONDS);
        }
    }

    private void filterHealthyInstances(Collection<Instance> instances) {
        filterData(instances, Instance::isEnabled);
    }

    private <T> void filterData(Collection<T> collection, NacosDataFilter<T> filter) {
        // remove if not accept
        collection.removeIf(data -> !filter.accept(data));
    }

    private Set<String> getServiceNames(URL url, NotifyListener listener) {
        if (isAdminProtocol(url)) {
            scheduleServiceNamesLookup(url, listener);
            return getServiceNamesForOps(url);
        } else {
            return getServiceNames0(url);
        }
    }

    private Set<String> getServiceNamesForOps(URL url) {
        Set<String> serviceNames = getAllServiceNames();
        filterServiceNames(serviceNames, url);
        return serviceNames;
    }

    private Set<String> getServiceNames0(URL url) {
        NacosServiceName serviceName = createServiceName(url);

        final Set<String> serviceNames;

        if (serviceName.isConcrete()) { // is the concrete service name
            serviceNames = singleton(serviceName.toString());
        } else {
            serviceNames = filterServiceNames(serviceName);
        }

        return serviceNames;
    }

    private NacosServiceName createServiceName(URL url) {
        return valueOf(url);
    }

    private Set<String> filterServiceNames(NacosServiceName serviceName) {
        Set<String> serviceNames = new LinkedHashSet<>();

        execute(namingService -> {

            serviceNames.addAll(namingService.getServicesOfServer(1, Integer.MAX_VALUE, groupName).getData()
                    .stream()
                    .map(NacosServiceName::new)
                    .filter(serviceName::isCompatible)
                    .map(NacosServiceName::toString)
                    .collect(Collectors.toList()));

        });

        return serviceNames;
    }

    private String[] getCategories(URL url) {
        return ANY_VALUE.equals(url.getServiceInterface()) ?
                ALL_SUPPORTED_CATEGORIES : of(DEFAULT_CATEGORY);
    }

    private void filterServiceNames(Set<String> serviceNames, URL url) {

        final String[] categories = getCategories(url);

        final String targetServiceInterface = url.getServiceInterface();

        final String targetVersion = url.getParameter(VERSION_KEY, "");

        final String targetGroup = url.getParameter(GROUP_KEY, "");

        filterData(serviceNames, serviceName -> {
            // split service name to segments
            // (required) segments[0] = category
            // (required) segments[1] = serviceInterface
            // (optional) segments[2] = version
            // (optional) segments[3] = group
            String[] segments = StringUtils.split(serviceName, SERVICE_NAME_SEPARATOR);
            int length = segments.length;
            if (length != 4) { // must present 4 segments
                return false;
            }

            String category = segments[CATEGORY_INDEX];
            if (!ArrayUtils.contains(categories, category)) { // no match category
                return false;
            }

            String serviceInterface = segments[SERVICE_INTERFACE_INDEX];
            if (!WILDCARD.equals(targetServiceInterface) &&
                    !StringUtils.equals(targetServiceInterface, serviceInterface)) { // no match service interface
                return false;
            }

            String version = segments[SERVICE_VERSION_INDEX];
            if (!WILDCARD.equals(targetVersion) &&
                    !StringUtils.equals(targetVersion, version)) { // no match service version
                return false;
            }

            String group = segments[SERVICE_GROUP_INDEX];
            return group == null || WILDCARD.equals(targetGroup)
                    || StringUtils.equals(targetGroup, group);
        });
    }

    private boolean isAdminProtocol(URL url) {
        return ADMIN_PROTOCOL.equals(url.getProtocol());
    }

    private List<URL> buildURLs(URL consumerURL, Collection<Instance> instances) {
        if (instances.isEmpty()) {
            return Collections.emptyList();
        }
        List<URL> urls = new LinkedList<>();
        for (Instance instance : instances) {
            URL url = buildURL(instance);
            if (UrlUtils.isMatch(consumerURL, url)) {
                urls.add(url);
            }
        }
        return urls;
    }

    private URL buildURL(Instance instance) {
        Map<String, String> metadata = instance.getMetadata();
        String protocol = metadata.get(PROTOCOL_KEY);
        String path = metadata.get(PATH_KEY);
        return new URL(protocol,
                instance.getIp(),
                instance.getPort(),
                path,
                instance.getMetadata());
    }

    private String getServiceName(URL url) {
        return getServiceName(url, url.getParameter(CATEGORY_KEY, DEFAULT_CATEGORY));
    }

    private String getServiceName(URL url, String category) {
        return category + SERVICE_NAME_SEPARATOR + url.getColonSeparatedKey();
    }

    private Set<String> getAllServiceNames() {

        final Set<String> serviceNames = new LinkedHashSet<>();

        execute(namingService -> {

            int pageIndex = 1;
            ListView<String> listView = namingService.getServicesOfServer(pageIndex, PAGINATION_SIZE, groupName);
            // First page data
            List<String> firstPageData = listView.getData();
            // Append first page into list
            serviceNames.addAll(firstPageData);
            // the total count
            int count = listView.getCount();
            // the number of pages
            int pageNumbers = count / PAGINATION_SIZE;
            int remainder = count % PAGINATION_SIZE;
            // remain
            if (remainder > 0) {
                pageNumbers += 1;
            }
            // If more than 1 page
            while (pageIndex < pageNumbers) {
                listView = namingService.getServicesOfServer(++pageIndex, PAGINATION_SIZE, groupName);
                serviceNames.addAll(listView.getData());
            }

        });

        return serviceNames;
    }

    private Instance createInstance(URL url) {
        // Append default category if absent
        String category = url.getParameter(CATEGORY_KEY, DEFAULT_CATEGORY);
        URL newURL = url.addParameter(CATEGORY_KEY, category);
        newURL = newURL.addParameter(PROTOCOL_KEY, url.getProtocol());
        newURL = newURL.addParameter(PATH_KEY, url.getPath());
        String ip = url.getHost();
        int port = url.getPort();
        Instance instance = new Instance();
        instance.setIp(ip);
        instance.setPort(port);
        instance.setMetadata(new HashMap<>(newURL.getParameters()));
        return instance;
    }

    private void execute(NamingServiceCallback callback) {
        try {
            callback.callback(namingService);
        } catch (NacosException e) {
            if (logger.isErrorEnabled()) {
                logger.error(e.getErrMsg(), e);
            }
        }
    }

    @SafeVarargs
    private static <T> T[] of(T... values) {
        return values;
    }

    private interface NacosDataFilter<T> {

        /**
         * Tests whether or not the specified data should be accepted.
         *
         * @param data The data to be tested
         * @return <code>true</code> if and only if <code>data</code>
         * should be accepted
         */
        boolean accept(T data);

    }

    interface NamingServiceCallback {

        /**
         * Callback
         *
         * @param namingService {@link NamingService}
         * @throws NacosException
         */
        void callback(NamingService namingService) throws NacosException;

    }

}
