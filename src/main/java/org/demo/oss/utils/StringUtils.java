package org.demo.oss.utils;

import java.util.List;

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

    /**
     * 将list列表转换成字符串，以逗号分隔
     * @param list 列表
     * @param  separator 分隔符
     * @param  prefix 前缀
     * @param  suffix 后缀
     * @return 字符串
     */
    public static String join(List<?> list, String separator, String prefix, String suffix) {
        StringBuilder sb = new StringBuilder();
        if (!isBlank(prefix)) {
            sb.append(prefix);
        }
        if (list != null && !list.isEmpty() && !isBlank(separator)) {
            for (Object obj : list) {
                sb.append(obj);
                sb.append(separator);
            }
            sb.delete(sb.length() - separator.length(), sb.length());
        }
        if (!isBlank(suffix)) {
            sb.append(suffix);
        }
        return sb.toString();
    }
}
