package org.demo.oss.utils;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Spring工具类
 */
@Component
@Slf4j
public class SpringUtils implements BeanFactoryAware, EnvironmentAware {

    private static BeanFactory bean;

    private static Environment env;

    @Override
    public void setBeanFactory(@NotNull BeanFactory beanFactory) throws BeansException {
        bean = beanFactory;
    }

    @Override
    public void setEnvironment(@NotNull Environment environment) {
        env = environment;
    }

    /**
     * 根据bean的class获取bean
     * @param clazz bean的class
     * @return bean
     */
    public static <T> T getBean(Class<T> clazz) {
        return clazz == null ? null : bean.getBean(clazz);
    }

    /**
     * 获取当前属性名对应的属性值
     * @param key 属性名
     * @return 属性值
     */
    public static String getProperty(String key) {
        return env.getProperty(key);
    }

    /**
     * 获取当前环境的ip
     * @return 当前ip
     */
    public static String getHost(){
        try {
            return InetAddress.getLocalHost().getHostAddress() + ":" + env.getProperty("server.port");
        } catch (UnknownHostException e) {
            throw new RuntimeException("获取当前环境的ip失败");
        }
    }
}
