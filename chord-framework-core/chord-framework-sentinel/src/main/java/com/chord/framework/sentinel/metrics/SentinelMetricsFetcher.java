package com.chord.framework.sentinel.metrics;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.node.metric.MetricNode;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * 获取sentinel的监控信息的工具类
 *
 * Created on 2020/8/5
 *
 * @author: wulinfeng
 */
public class SentinelMetricsFetcher implements EnvironmentAware {

    private static final Logger logger = LoggerFactory.getLogger(SentinelMetricsFetcher.class);

    private static final String METRIC_URL_PATH = "metric";

    private static final String METRIC_URL_PORT = "8719";

    public static final String NO_METRICS = "No metrics";

    public static final long DEFAULT_DURATION = 1000;

    private static final int HTTP_OK = 200;

    private String machine;

    private Environment environment;

    private CloseableHttpAsyncClient httpclient;

    private List<MetricEntity> metricEntities = new ArrayList<>();

    private volatile AtomicLong lastFetchTime = new AtomicLong(0);

    private final long duration;

    public SentinelMetricsFetcher() throws UnknownHostException {
        this(null, DEFAULT_DURATION);
    }

    public SentinelMetricsFetcher(String machine, long duration) throws UnknownHostException {

        IOReactorConfig ioConfig = IOReactorConfig.custom()
                .setConnectTimeout(3000)
                .setSoTimeout(3000)
                .setIoThreadCount(Runtime.getRuntime().availableProcessors() * 2)
                .build();

        httpclient = HttpAsyncClients.custom()
                .setRedirectStrategy(new DefaultRedirectStrategy() {
                    @Override
                    protected boolean isRedirectable(final String method) {
                        return false;
                    }
                }).setMaxConnTotal(4000)
                .setMaxConnPerRoute(1000)
                .setDefaultIOReactorConfig(ioConfig)
                .build();

        httpclient.start();

        if(machine == null) {
            InetAddress addr = InetAddress.getLocalHost();
            this.machine = addr.getHostAddress();
        }

        this.duration = duration;

    }

    public List<MetricEntity> fetch(long startTime, long endTime) {
        return fetch(machine, startTime, endTime);

    }

    public List<MetricEntity> fetch(String ip, long startTime, long endTime) {

        // 监控信息缓存，防止过多的请求
        if((endTime - lastFetchTime.get() < duration) && (endTime - lastFetchTime.get() > 0)) {
            return metricEntities;
        }

        // 保存监控结果
        final List<MetricEntity> metricList = new ArrayList<>();

        synchronized (this) {

            final String url = "http://" + ip + ":" + METRIC_URL_PORT + "/" + METRIC_URL_PATH
                    + "?startTime=" + startTime + "&endTime=" + endTime + "&refetch=" + false;

            final CountDownLatch latch = new CountDownLatch(1);

            final HttpGet httpGet = new HttpGet(url);

            httpclient.execute(httpGet, new FutureCallback<HttpResponse>() {
                @Override
                public void completed(final HttpResponse response) {
                    try {
                        handleResponse(response, ip, metricList);
                    } catch (Exception e) {
                        logger.error(" metric " + url + " error:", e);
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void failed(final Exception ex) {
                    latch.countDown();
                    httpGet.abort();
                    if (ex instanceof SocketTimeoutException) {
                        logger.error("Failed to fetch metric from <{}>: socket timeout", url);
                    } else if (ex instanceof ConnectException) {
                        logger.error("Failed to fetch metric from <{}> (ConnectionException: {})", url, ex.getMessage());
                    } else {
                        logger.error(" metric " + url + " error", ex);
                    }
                }

                @Override
                public void cancelled() {
                    latch.countDown();
                    httpGet.abort();
                }
            });

            try {
                latch.await(15 * 1000, TimeUnit.SECONDS);
            } catch (Exception e) {
                logger.info("metric, wait http client error:", e);
            }

            metricEntities = metricList;
            lastFetchTime.set(System.currentTimeMillis());

        }
        return metricList;

    }

    private void handleResponse(HttpResponse response, String machine, List<MetricEntity> metricList) throws IOException {
        int code = response.getStatusLine().getStatusCode();
        if (code != HTTP_OK) {
            return;
        }
        Charset charset = null;
        try {
            String contentTypeStr = response.getFirstHeader("Content-type").getValue();
            if (StringUtil.isNotEmpty(contentTypeStr)) {
                ContentType contentType = ContentType.parse(contentTypeStr);
                charset = contentType.getCharset();
            }
        } catch (Exception ignore) {
        }
        String body = EntityUtils.toString(response.getEntity(), charset != null ? charset : Charset.forName("UTF-8"));
        if (StringUtil.isEmpty(body) || body.startsWith(NO_METRICS)) {
            return;
        }
        String[] lines = body.split("\n");
        handleBody(lines, metricList);
    }

    private void handleBody(String[] lines, List<MetricEntity> list) {
        if (lines.length < 1) {
            return;
        }

        String appName = environment.getProperty("application.name");

        for (String line : lines) {
            try {
                MetricNode node = MetricNode.fromThinString(line);
                // 聚合指标不作为监控数据
                if (shouldFilterOut(node.getResource())) {
                    continue;
                }
                MetricEntity entity = new MetricEntity();
                entity.setResource(node.getResource());
                entity.setPassQps(node.getPassQps());
                entity.setBlockQps(node.getBlockQps());
                entity.setRt(node.getRt());
                entity.setSuccessQps(node.getSuccessQps());
                entity.setExceptionQps(node.getExceptionQps());
                entity.setCount(1);
                list.add(entity);
            } catch (Exception e) {
                logger.warn("handleBody line exception, machine: {}, line: {}",  line);
            }
        }
    }

    private boolean shouldFilterOut(String resource) {
        return RES_EXCLUSION_SET.contains(resource);
    }

    private static final Set<String> RES_EXCLUSION_SET = new HashSet<String>() {{
        add(Constants.TOTAL_IN_RESOURCE_NAME);
        add(Constants.SYSTEM_LOAD_RESOURCE_NAME);
        add(Constants.CPU_USAGE_RESOURCE_NAME);
    }};

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
