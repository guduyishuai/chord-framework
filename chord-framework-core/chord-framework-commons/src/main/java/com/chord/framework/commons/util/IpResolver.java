package com.chord.framework.commons.util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * Created on 2020/7/6
 *
 * @author: wulinfeng
 */
public class IpResolver {

    /**负载均衡作用的代理，比如apache，nginx，非RFC标准，squid中有相关定义**/
    private static String HTTP_HEADER_FORWARDED = "x-forwarded-for";

    /**同x-forwarded-for**/
    private static String HTTP_HEADER_X_FORWARDED = "HTTP-X-FORWARDED_FOR";

    /**apache+WebLogic**/
    private static String HTTP_HEADER_PROXY = "Proxy-Client";

    /**同Proxy-Client**/
    private static String HTTP_HEADER_WL_PROXY = "WL-Proxy-Client";

    /**代理服务器可能会添加该请求头**/
    private static String HTTP_HEADER_CLIENT = "HTTP-CLIENT-IP";

    /**nginx设置了才有**/
    private static String HTTP_HEADER_X_REAL = "HTTP-X-REAL-IP";

    private static String UNKNOW = "unknow";

    private static String IP_SPLITER = ",";

    /**
     *
     * 只能获取不使用代理，使用透明代理的真实ip
     * 对于普通匿名代理，欺骗性代理，高匿名代理没办法
     *
     * @return
     */
    public static String getIp() {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String ip = request.getHeader(HTTP_HEADER_FORWARDED);
        if(ip != null && ip.length() != 0 && !UNKNOW.equalsIgnoreCase(ip)) {
            return ip.split(IP_SPLITER)[0];
        }

        ip = request.getHeader(HTTP_HEADER_X_FORWARDED);
        if(ip != null && ip.length() != 0 && !UNKNOW.equalsIgnoreCase(ip)) {
            return ip.split(IP_SPLITER)[0];
        }

        ip = request.getHeader(HTTP_HEADER_PROXY);
        if(ip != null && !UNKNOW.equalsIgnoreCase(ip)) {
            return ip;
        }

        ip = request.getHeader(HTTP_HEADER_WL_PROXY);
        if(ip != null && !UNKNOW.equalsIgnoreCase(ip)) {
            return ip;
        }

        ip = request.getHeader(HTTP_HEADER_CLIENT);
        if(ip != null && !UNKNOW.equalsIgnoreCase(ip)) {
            return ip;
        }

        ip = request.getHeader(HTTP_HEADER_X_REAL);
        if(ip != null && !UNKNOW.equalsIgnoreCase(ip)) {
            return ip;
        }

        return request.getRemoteAddr();

    }

    public static ClientProxy getProxy() {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String forwardIp = request.getHeader(HTTP_HEADER_FORWARDED);
        String clientIp = request.getHeader(HTTP_HEADER_CLIENT);
        String remoteIp = request.getRemoteAddr();

        if(forwardIp == null || UNKNOW.equalsIgnoreCase(forwardIp) || forwardIp.length() == 0) {
            if(clientIp == null || UNKNOW.equalsIgnoreCase(clientIp)){
                if(remoteIp != null && !UNKNOW.equalsIgnoreCase(remoteIp)) {
                    return ClientProxy.NONE_OR_HIGH_ANONYMITY_PROXIES;
                }
            }
        } else {
            if(forwardIp.split(IP_SPLITER)[0].equalsIgnoreCase(remoteIp)) {
                return ClientProxy.ANONYMOUS_PROXIES;
            } else {
                return ClientProxy.TRANSPARENT_OR_DISTORTING_PROXIES;
            }
        }

        return ClientProxy.KNOWN;

    }

    enum ClientProxy {

        /**无代理或者高匿名代理**/
        NONE_OR_HIGH_ANONYMITY_PROXIES,
        /**透明代理或者欺诈性代理**/
        TRANSPARENT_OR_DISTORTING_PROXIES,
        /**匿名代理**/
        ANONYMOUS_PROXIES,
        /**未知**/
        KNOWN

    }

}
