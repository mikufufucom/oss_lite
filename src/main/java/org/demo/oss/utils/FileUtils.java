package org.demo.oss.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * 文件工具类
 */
@Slf4j
public class FileUtils {

    /**
     * 字节转换
     * @param size 字节大小
     * @return {@link String} 转换后的文件大小
     */
    public static String convertFileSize(long size)
    {
        int length = String.valueOf(size).length();
        if (length>12){
            throw new RuntimeException("文件超过1TB");
        }
        if (length < 4) {
            return size + "B";
        } else if (length < 7) {
            return String.format("%.2f", size / 1024.0) + "KB";
        } else if (length < 10) {
            return String.format("%.2f", size / (1024*1024.0) ) + "MB";
        } else {
            return String.format("%.2f", size / (1024*1024*1024.0) ) + "GB";
        }
    }
}
