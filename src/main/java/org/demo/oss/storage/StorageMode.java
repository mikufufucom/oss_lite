package org.demo.oss.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 存储模式接口类
 */
public interface StorageMode {

    /**
     * 文件上传
     * @param multipartFile 文件
     * @param pathName 文件路径
     * @param objectName 文件名
     * @return 文件资源链接
     */
    String upload(MultipartFile multipartFile,String pathName,String objectName);

    /**
     * 文件上传
     * @param inputStream 文件
     * @param pathName 文件路径
     * @param objectName 文件名
     * @return 文件资源链接
     */
    String upload(InputStream inputStream,String pathName,String objectName);

    /**
     * 文件下载 外链
     * @param objectName 文件名
     * @return 文件的二进制流
     */
    InputStream download(String objectName);

    /**
     * 文件删除 外链
     * @param fileName 文件名
     * @return flag 删除成功的标志
     */
    Boolean delete(String fileName);

    /**
     * 获取文件的外链
     * @param objectName 文件名
     * @return 文件的外链
     */
    String getObjectUrl(String objectName);

    /**
     * 获取该前缀的对象列表信息 包括子目录下的对象
     * @param objectNamePrefix 对象名前缀
     * @param isSubDir 是否包含子目录
     * @return 对象列表信息
     */
    List<Map<String,String>> listObjects(String objectNamePrefix, Boolean isSubDir);
}
