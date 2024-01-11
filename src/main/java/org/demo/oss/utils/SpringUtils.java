package org.demo.oss.utils;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.stereotype.Component;

/**
 * Spring工具类
 */
@Component
public class SpringUtils implements BeanFactoryAware {

    private static BeanFactory bean;

    @Override
    public void setBeanFactory(@NotNull BeanFactory beanFactory) throws BeansException {
        bean = beanFactory;
    }

    /**
     * 根据bean的class获取bean
     * @param clazz bean的class
     * @return bean
     */
    public static <T> T getBean(Class<T> clazz) {
        return clazz == null ? null : bean.getBean(clazz);
    }
}
