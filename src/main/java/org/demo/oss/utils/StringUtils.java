package org.demo.oss.utils;

/**
 * 字符串工具类
 */
public class StringUtils {

    private StringUtils(){}

    /**
     * 判断字符串是否为空白
     * @param str 字符串
     * @return true=字符串为空白；false=字符串不为空白
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}
