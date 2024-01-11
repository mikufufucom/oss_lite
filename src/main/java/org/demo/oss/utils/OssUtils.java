package org.demo.oss.utils;

import org.demo.oss.config.OssConfig;
import org.demo.oss.config.entity.OssProp;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.ListObjectsRequest;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 阿里OSS 工具类
 */
@Slf4j
public class OssUtils {

    private static final  SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 获取配置的阿里OSS客户端
     * @return 阿里OSS客户端
     */
    private static OSS getOssClient(){
        return SpringUtils.getBean(OssConfig.class).ossClient();
    }

    private static OssProp getOssProp(){
        return SpringUtils.getBean(OssConfig.class).getOssProp();
    }

    /**
     * 阿里OSS服务 上传文件
     * @param multipartFile spring的封装文件流
     * @param pathName 文件路径
     * @param objectName 文件名称
     * @return 文件上传路径
     */
    public static String upload(MultipartFile multipartFile,String pathName,String objectName){
        if (null == multipartFile || 0 == multipartFile.getSize()) {
            return null;
        }
        OSS ossClient = getOssClient();
        try {
            ossClient.putObject(
                    getOssProp().getBucketName(),
                    pathName + "/" + objectName,
                    multipartFile.getInputStream()
            );
            if (StringUtils.isBlank(pathName)) {
                return getOssProp().getHost() + "/" + objectName;
            }
            return getOssProp().getHost() + "/" + pathName + "/" + objectName;
        } catch (OSSException oe){
            log.error("捕获到OSSException，这意味着您的请求已发送到OSS， "
                    + "但是由于某种原因以错误响应被拒绝。");
            log.error("Error Message:" + oe.getErrorMessage());
            log.error("Error Code:" + oe.getErrorCode());
            log.error("Request ID:" + oe.getRequestId());
            log.error("Host ID:" + oe.getHostId());
        }catch (ClientException ce){
            log.error("捕获ClientException，这意味着客户端遇到"
                    + "在尝试与OSS通信时出现了严重的内部问题，"
                    + "例如不能接入网络。");
            log.error("Error Message:" + ce.getMessage());
        }catch (IOException ioe){
            log.error("捕获IOException，这意味着客户端遇到"
                    + "在尝试与OSS通信时出现了严重的内部问题，"
                    + "例如不能接入网络。");
            log.error("Error Message:" + ioe.getMessage());
        }
        return null;
    }

    /**
     * 阿里OSS服务 删除文件
     * @param objectName 文件名称 有路径需要带路径
     * @return 删除成功返回true，失败返回false
     */
    public static Boolean delete(String objectName) {
        OSS ossClient = getOssClient();
        try {
            ossClient.deleteObject(getOssProp().getBucketName(), objectName);
            return true;
        } catch (OSSException oe) {
            log.error("捕获到OSSException，这意味着您的请求已发送到OSS， "
                    + "但是由于某种原因以错误响应被拒绝。");
            log.error("Error Message:" + oe.getErrorMessage());
            log.error("Error Code:" + oe.getErrorCode());
            log.error("Request ID:" + oe.getRequestId());
            log.error("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            log.error("捕获ClientException，这意味着客户端遇到"
                    + "在尝试与OSS通信时出现了严重的内部问题，"
                    + "例如不能接入网络。");
            log.error("Error Message:" + ce.getMessage());
        }
        return false;
    }

    /**
     * 阿里OSS服务 下载文件
     * @param objectName 文件名称 有路径需要带路径
     * @return 文件的二进制流
     */
    public static InputStream download(String objectName) {
        OSS ossClient = getOssClient();
        try {
            return ossClient.getObject(getOssProp().getBucketName(), objectName).getObjectContent();
        } catch (OSSException oe) {
            log.error("捕获到OSSException，这意味着您的请求已发送到OSS， "
                    + "但是由于某种原因以错误响应被拒绝。");
            log.error("Error Message:" + oe.getErrorMessage());
            log.error("Error Code:" + oe.getErrorCode());
            log.error("Request ID:" + oe.getRequestId());
            log.error("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            log.error("捕获ClientException，这意味着客户端遇到"
                    + "在尝试与OSS通信时出现了严重的内部问题，"
                    + "例如不能接入网络。");
            log.error("Error Message:" + ce.getMessage());
        }
        return null;
    }

    /**
     * 阿里OSS服务 获取文件外链
     * @param objectName 文件名称 有路径需要带路径
     * @param duration 有效大小 例如 7 表示7个单位时间
     * @param unit TimeUnit 有效时间的单位 例如 DAYS表示天数
     * @return 文件外链
     */
    public static String getObjectUrl(String objectName, Integer duration, TimeUnit unit) {
        OSS ossClient = getOssClient();
        try {
            return ossClient.generatePresignedUrl(getOssProp().getBucketName(),
                    objectName,
                    new Date(System.currentTimeMillis() + unit.toMillis(duration))
            ).toString();
        } catch (OSSException oe) {
            log.error("捕获到OSSException，这意味着您的请求已发送到OSS， "
                    + "但是由于某种原因以错误响应被拒绝。");
            log.error("Error Message:" + oe.getErrorMessage());
            log.error("Error Code:" + oe.getErrorCode());
            log.error("Request ID:" + oe.getRequestId());
            log.error("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            log.error("捕获ClientException，这意味着客户端遇到"
                    + "在尝试与OSS通信时出现了严重的内部问题，"
                    + "例如不能接入网络。");
            log.error("Error Message:" + ce.getMessage());
        }
        return null;
    }

    /**
     * 阿里OSS服务 获取文件外链 默认7天
     * @param objectName 文件名称 有路径需要带路径
     * @return 文件外链
     */
    public static String getObjectUrl(String objectName) {
        return getObjectUrl(objectName, 7, TimeUnit.DAYS);
    }

    /**
     * 阿里OSS服务 列出桶的对象列表信息
     * @param objectNamePrefix 对象名前缀
     * @param maxKeys 最大值
     * @return 对象列表信息
     */
    public static List<Map<String,String>> listObjects(String objectNamePrefix, Integer maxKeys) {
        OSS ossClient = getOssClient();
        try {
            ListObjectsRequest listObjectsRequest = new ListObjectsRequest(getOssProp().getBucketName())
                    .withPrefix(objectNamePrefix)
                    .withMaxKeys(maxKeys);
            ObjectListing objectListing = ossClient.listObjects(listObjectsRequest);
            List<OSSObjectSummary> sums = objectListing.getObjectSummaries();
            return sums.stream().map(ossObject -> {
                Map<String,String> map = new HashMap<>();
                map.put("name",ossObject.getKey());
                map.put("url",getOssProp().getHost() + "/" + ossObject.getKey());
                map.put("size",FileUtils.convertFileSize(ossObject.getSize()));
                map.put("lastModified",SIMPLE_DATE_FORMAT.format(ossObject.getLastModified()));
                return map;
            }).collect(Collectors.toList());
        } catch (OSSException oe) {
            log.error("捕获到OSSException，这意味着您的请求已发送到OSS， "
                    + "但是由于某种原因以错误响应被拒绝。");
            log.error("Error Message:" + oe.getErrorMessage());
            log.error("Error Code:" + oe.getErrorCode());
            log.error("Request ID:" + oe.getRequestId());
            log.error("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            log.error("捕获ClientException，这意味着客户端遇到"
                    + "在尝试与OSS通信时出现了严重的内部问题，"
                    + "例如不能接入网络。");
            log.error("Error Message:" + ce.getMessage());
        }
        return null;
    }

    /**
     * 阿里OSS服务 列出桶的对象列表信息 默认100条
     * @param objectNamePrefix 对象名前缀
     * @return 对象列表信息
     */
    public static List<Map<String,String>> listObjects(String objectNamePrefix) {
        return listObjects(objectNamePrefix, 100);
    }
}
