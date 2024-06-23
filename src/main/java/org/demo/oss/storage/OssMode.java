package org.demo.oss.storage;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.ListObjectsRequest;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;
import lombok.extern.slf4j.Slf4j;
import org.demo.oss.model.Storage;
import org.demo.oss.service.StorageService;
import org.demo.oss.utils.FileUtils;
import org.demo.oss.utils.SpringUtils;
import org.demo.oss.utils.StringUtils;
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
 * 阿里OSS存储模式实现类
 * @author moxiaoli
 */
@Slf4j
public class OssMode implements StorageMode {

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 获取配置的阿里OSS客户端
     * @return 阿里OSS客户端
     */
    public OSS getOssClient(){
        Storage storage = getOssProp();
        if (storage != null) {
            return new OSSClientBuilder().build(
                    storage.getEndpoint(),
                    storage.getAccessKey(),
                    storage.getSecretKey()
            );
        }
        throw new RuntimeException("未配置minio");
    }

    private Storage getOssProp(){
        return SpringUtils.getBean(StorageService.class).getStorage();
    }

    @Override
    public String upload(MultipartFile multipartFile) {
        // 判断上传文件是否为空
        if (null == multipartFile || 0 == multipartFile.getSize()) {
            throw new RuntimeException("文件不能为空");
        }
        return upload(multipartFile, multipartFile.getOriginalFilename());
    }

    @Override
    public String upload(MultipartFile multipartFile, String objectName) {
        if(isExist(objectName)){
            return getObjectUrlLong(objectName);
        }else {
            // 判断上传文件是否为空
            if (null == multipartFile || 0 == multipartFile.getSize()) {
                throw new RuntimeException("文件不能为空");
            }
            return upload(multipartFile, "", objectName);
        }
    }

    /**
     * 阿里OSS 判断文件是否已存在
     *  @param objectName 文件名称
     * @return 文件是否存在 如果返回值为true，则文件存在，否则存储空间或者文件不存在。
     */
    public boolean isExist(String objectName){
        try {
            // 判断oss上文件是否存在
            return getOssClient().doesObjectExist(getOssProp().getBucketName(), objectName);
        }catch (OSSException oe){
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
        }
        return false;
    }

    @Override
    public String upload(String pathName, MultipartFile multipartFile) {
        // 判断上传文件是否为空
        if (null == multipartFile || 0 == multipartFile.getSize()) {
            throw new RuntimeException("文件不能为空");
        }
        return upload(multipartFile, pathName, multipartFile.getOriginalFilename());
    }

    @Override
    public String upload(MultipartFile multipartFile, String pathName, String objectName) {
        // 判断上传文件是否为空
        if (null == multipartFile || 0 == multipartFile.getSize()) {
            throw new RuntimeException("文件不能为空");
        }
        InputStream inputStream = null;
        try {
            inputStream = multipartFile.getInputStream();
            // 上传文件
            getOssClient().putObject(
                    // 存储空间
                    getOssProp().getBucketName(),
                    // 上传的文件名
                    pathName + "/" + objectName,
                    // 上传文件的输入流
                    inputStream
            );
            // 返回文件上传路径
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
            throw new RuntimeException("上传失败");
        }catch (ClientException ce){
            log.error("捕获ClientException，这意味着客户端遇到"
                    + "在尝试与OSS通信时出现了严重的内部问题，"
                    + "例如不能接入网络。");
            log.error("Error Message:" + ce.getMessage());
            throw new RuntimeException("上传失败");
        }catch (IOException ioe){
            log.error("捕获IOException，这意味着客户端遇到"
                    + "在尝试与OSS通信时出现了严重的内部问题，"
                    + "例如不能接入网络。");
            log.error("Error Message:" + ioe.getMessage());
            throw new RuntimeException("上传失败");
        }finally {
            if (null != inputStream){
                try {
                    inputStream.close();
                }catch (Exception e){
                    log.error("文件流关闭失败：{}",e.getMessage());
                }
            }
        }
    }

    @Override
    public String upload(InputStream inputStream, String pathName, String objectName) {
        try {
            getOssClient().putObject(
                    getOssProp().getBucketName(),
                    pathName + "/" + objectName,
                    inputStream
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
            throw new RuntimeException("上传失败");
        }catch (ClientException ce){
            log.error("捕获ClientException，这意味着客户端遇到"
                    + "在尝试与OSS通信时出现了严重的内部问题，"
                    + "例如不能接入网络。");
            log.error("Error Message:" + ce.getMessage());
            throw new RuntimeException("上传失败");
        } finally {
            if (null != inputStream){
                try {
                    inputStream.close();
                }catch (Exception e){
                    log.error("文件流关闭失败：{}",e.getMessage());
                }
            }
        }
    }

    @Override
    public InputStream download(String objectName) {
        try {
            // 下载文件
            return getOssClient().getObject(getOssProp().getBucketName(), objectName).getObjectContent();
        } catch (OSSException oe) {
            log.error("捕获到OSSException，这意味着您的请求已发送到OSS， "
                    + "但是由于某种原因以错误响应被拒绝。");
            log.error("Error Message:" + oe.getErrorMessage());
            log.error("Error Code:" + oe.getErrorCode());
            log.error("Request ID:" + oe.getRequestId());
            log.error("Host ID:" + oe.getHostId());
            throw new RuntimeException("下载文件失败");
        } catch (ClientException ce) {
            log.error("捕获ClientException，这意味着客户端遇到"
                    + "在尝试与OSS通信时出现了严重的内部问题，"
                    + "例如不能接入网络。");
            log.error("Error Message:" + ce.getMessage());
            throw new RuntimeException("下载文件失败");
        }
    }

    @Override
    public Boolean delete(String objectName) {
        try {
            // 删除文件
            getOssClient().deleteObject(getOssProp().getBucketName(), objectName);
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

    @Override
    public String getObjectUrl(String objectName) {
        return getObjectUrl(objectName, 7, TimeUnit.DAYS);
    }

    @Override
    public String getObjectUrl(String objectName, Integer duration, TimeUnit unit) {
        try {
            return getOssClient().generatePresignedUrl(getOssProp().getBucketName(),
                    objectName,
                    // 设置URL过期时间
                    new Date(System.currentTimeMillis() + unit.toMillis(duration))
            ).toString();
        } catch (OSSException oe) {
            log.error("捕获到OSSException，这意味着您的请求已发送到OSS， "
                    + "但是由于某种原因以错误响应被拒绝。");
            log.error("Error Message:" + oe.getErrorMessage());
            log.error("Error Code:" + oe.getErrorCode());
            log.error("Request ID:" + oe.getRequestId());
            log.error("Host ID:" + oe.getHostId());
            throw new RuntimeException("获取外链失败");
        } catch (ClientException ce) {
            log.error("捕获ClientException，这意味着客户端遇到"
                    + "在尝试与OSS通信时出现了严重的内部问题，"
                    + "例如不能接入网络。");
            log.error("Error Message:" + ce.getMessage());
            throw new RuntimeException("获取外链失败");
        }
    }

    @Override
    public String getObjectUrlLong(String objectName) {
        try {
            // 判断文件是否存在。如果返回值为true，则文件存在，否则存储空间或者文件不存在。
            boolean exist = getOssClient().doesObjectExist(getOssProp().getBucketName(), objectName);
            if (exist) {
                // 获取文件外链
                return getOssProp().getHost() + "/" + objectName;
            }
            throw new RuntimeException("文件不存在");
        } catch (OSSException oe) {
            log.error("捕获到OSSException，这意味着您的请求已发送到OSS， "
                    + "但是由于某种原因以错误响应被拒绝。");
            log.error("Error Message:" + oe.getErrorMessage());
            log.error("Error Code:" + oe.getErrorCode());
            log.error("Request ID:" + oe.getRequestId());
            log.error("Host ID:" + oe.getHostId());
            throw new RuntimeException("获取外链失败");
        } catch (ClientException ce) {
            log.error("捕获ClientException，这意味着客户端遇到"
                    + "在尝试与OSS通信时出现了严重的内部问题，"
                    + "例如不能接入网络。");
            log.error("Error Message:" + ce.getMessage());
            throw new RuntimeException("获取外链失败");
        }
    }

    @Override
    public List<Map<String, String>> listObjects(String objectNamePrefix, Boolean isSubDir) {
        return listObjects(objectNamePrefix, 100, isSubDir);
    }

    @Override
    public List<Map<String, String>> listObjects(String objectNamePrefix, Integer maxKeys, Boolean isSubDir) {
        try {
            ListObjectsRequest listObjectsRequest = new ListObjectsRequest(getOssProp().getBucketName())
                    // 列举文件。objectNamePrefix，则列举存储空间下的所有文件。objectNamePrefix，则列举包含指定前缀的文件。
                    .withPrefix(objectNamePrefix)
                    // 设置最大个数。
                    .withMaxKeys(maxKeys);
            ObjectListing objectListing = getOssClient().listObjects(listObjectsRequest);
            // 遍历所有文件。
            List<OSSObjectSummary> sums = objectListing.getObjectSummaries();
            // 获取该资源空间下所有objectName 例如：[test/1.txt, test/2.txt]
            // stream().map()方法是将list中的每一个元素映射成一个新的元素，然后将这些新的元素组成一个Stream流。
            // collect(Collectors.toList())方法是将流中的元素收集到List中。
//            return sums.stream().map(OSSObjectSummary::getKey).collect(Collectors.toList());
            return sums.stream().map(ossObject -> {
                Map<String,String> map = new HashMap<>();
                map.put("name",ossObject.getKey());
                map.put("url",getOssProp().getHost() + "/" + ossObject.getKey());
                map.put("size", FileUtils.convertFileSize(ossObject.getSize()));
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
            throw new RuntimeException("获取对象存储列表失败");
        } catch (ClientException ce) {
            log.error("捕获ClientException，这意味着客户端遇到"
                    + "在尝试与OSS通信时出现了严重的内部问题，"
                    + "例如不能接入网络。");
            log.error("Error Message:" + ce.getMessage());
            throw new RuntimeException("获取对象存储列表失败");
        }
//        finally {
//            // 关闭OSSClient。
//            if (ossClient != null) {
//                ossClient.shutdown();
//            }
//        }
    }

    @Override
    public List<Map<String, String>> listObjects(String objectNamePrefix) {
        return listObjects(objectNamePrefix, 100,false);
    }
}
