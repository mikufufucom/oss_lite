package org.demo.oss.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * 文件服务接口
 */
public interface UploadService {

    /**
     * 文件上传
     * @param multipartFile 文件流
     * @return 文件资源链接
     */
    String upload(MultipartFile multipartFile);

    /**
     * 文件上传
     * @param multipartFile 文件流
     * @param pathName 文件路径
     * @return 文件资源链接
     */
    String upload(MultipartFile multipartFile,String pathName);

    /**
     * 文件下载 外链
     * @param fileName 文件名
     * @return 文件资源链接
     */
    String download(String fileName);

    /**
     * 文件删除 外链
     * @param fileName 文件名
     * @return flag 删除成功的标志
     */
    Boolean delete(String fileName);

    /**
     * 图片下载二进制流
     * @param fileName 文件名
     * @param outputStream 输出流
     */
    void downloadImage(String fileName, OutputStream outputStream);

    /**
     * 获取该前缀的对象列表信息 包括子目录下的对象
     * @param objectNamePrefix 对象名前缀
     * @param isSubDir 是否包含子目录
     * @return 对象列表信息
     */
    List<Map<String,String>> listObjects(String objectNamePrefix, Boolean isSubDir);
}
