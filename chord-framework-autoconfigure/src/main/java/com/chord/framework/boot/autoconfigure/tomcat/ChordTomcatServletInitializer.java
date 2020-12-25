package com.chord.framework.boot.autoconfigure.tomcat;

import org.apache.catalina.core.ApplicationContext;
import org.apache.catalina.core.ApplicationContextFacade;
import org.apache.catalina.core.DefaultInstanceManager;
import org.apache.catalina.core.StandardContext;
import org.apache.tomcat.InstanceManager;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.web.SpringServletContainerInitializer;
import org.springframework.web.WebApplicationInitializer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 * filter或者servlet中不能使用&#064;Resource注解的原因是因为tomcat将该注解作为了jndi资源
 * 解决办法是修改{@link StandardContext}和{@link DefaultInstanceManager}的ignoreAnnotations属性
 * 解决{@link StandardContext}的设置问题，优先考虑修改context.xml
 * 这里要修改的ignoreAnnotations不能通过修改context.xml
 * 其实如果不用外部tomcat启动，spring自身会创建{@link TomcatServletWebServerFactory}，其中提供了{@link TomcatContextCustomizer}进行修改StandardContext的属性
 * 但是如果使用外部tomcat启动，就不是通过{@link TomcatWebServer}创建{@link ServletContextInitializer}，而是通过Tomcat提供的&#064;HandlesTypes机制
 * spring提供了一个{@link SpringServletContainerInitializer}用来获取{@link WebApplicationInitializer}扩展Tomcat
 *
 * @see SpringServletContainerInitializer
 * @see WebApplicationInitializer
 *
 * Created on 2020/12/22
 *
 * @author: wulinfeng
 */
public abstract class ChordTomcatServletInitializer extends SpringBootServletInitializer {

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        if(servletContext instanceof ApplicationContextFacade) {
            try {
                Field contextField = ApplicationContextFacade.class.getDeclaredField("context");
                if(!contextField.isAccessible()) {
                    contextField.setAccessible(true);
                }
                ApplicationContext context = (ApplicationContext) contextField.get(servletContext);
                Method contextMethod = ApplicationContext.class.getDeclaredMethod("getContext");
                if(!contextMethod.isAccessible()) {
                    contextMethod.setAccessible(true);
                }
                StandardContext standardContext = (StandardContext) contextMethod.invoke(context);
                standardContext.setIgnoreAnnotations(true);
                InstanceManager instanceManager = standardContext.getInstanceManager();
                Field instanceManagerField = DefaultInstanceManager.class.getDeclaredField("ignoreAnnotations");
                if(!instanceManagerField.isAccessible()) {
                    instanceManagerField.setAccessible(true);
                }
                instanceManagerField.setBoolean(instanceManager, Boolean.TRUE);
                standardContext.setInstanceManager(instanceManager);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        super.onStartup(servletContext);
    }

}
