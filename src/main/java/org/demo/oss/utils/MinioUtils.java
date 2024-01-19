package org.demo.oss.utils;


import lombok.extern.slf4j.Slf4j;
import org.demo.oss.config.OssConfig;
import org.demo.oss.config.entity.OssProp;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * minio工具类
 */
@Slf4j
public class MinioUtils {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 获取配置的Minio客户端
     * @return Minio客户端
     */
    private static MinioClient getMinioClient(){
        return SpringUtils.getBean(OssConfig.class).getMinioClient();
    }

    private static OssProp getOssProp(){
        return SpringUtils.getBean(OssConfig.class).getOssProp();
    }

    /**
     * minio对象存储 上传文件
     * @param multipartFile spring的封装文件流
     * @param pathName 文件路径
     * @param objectName 文件名称
     * @return 成功返回文件链接，失败返回失败的信息
     */
    public static String upload(MultipartFile multipartFile,String pathName,String objectName) {
        if (null == multipartFile || 0 == multipartFile.getSize()) {
            return null;
        }
        InputStream inputStream = null;
        try {
            inputStream = multipartFile.getInputStream();
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(getOssProp().getBucketName())
                    .object(pathName + "/" + objectName)
                    .contentType(multipartFile.getContentType())
                    .stream(inputStream, multipartFile.getSize(), -1)
                    .build();
            getMinioClient().putObject(putObjectArgs);
            if (StringUtils.isBlank(pathName)) {
                return getOssProp().getHost() + "/" + getOssProp().getBucketName() + "/" + objectName;
            }
            return getOssProp().getHost() + "/" + getOssProp().getBucketName() + "/" + pathName + "/" + objectName;
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
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

    /**
     * minio对象存储 上传文件
     * @param inputStream {@link InputStream} 文件流 实现时注意关闭流
     * @param pathName 文件路径
     * @param objectName 文件名称
     * @return 成功返回文件链接，失败返回失败的信息
     */
    public static String upload(InputStream inputStream,String pathName,String objectName){
        try {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(getOssProp().getBucketName())
                    .object(pathName + "/" + objectName)
                    .stream(inputStream,inputStream.available(),-1)
                    .build();
            getMinioClient().putObject(putObjectArgs);
            if (StringUtils.isBlank(pathName)) {
                return getOssProp().getHost() + "/" + getOssProp().getBucketName() + "/" + objectName;
            }
            return getOssProp().getHost() + "/" + getOssProp().getBucketName() + "/" + pathName + "/" + objectName;
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
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

    /**
     * minio对象存储 获取文件
     * @param objectName 文件名称 有路径需要带路径
     * @return 文件的二进制流
     */
    public static InputStream download(String objectName){
        try {
            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(getOssProp().getBucketName())
                    .object(objectName)
                    .build();
            return getMinioClient().getObject(getObjectArgs);
        }catch (Exception e){
            log.error(e.getMessage());
            return null;
        }
    }

    /**
     * minio对象存储 获取文件外链
     * 这里的 method 方法决定最后链接是什么请求获得
     *  expiry 决定这个链接多久失效
     * @param objectName 文件名称
     * @param duration 有效大小 例如 7 表示7个单位时间
     * @param unit TimeUnit 有效时间的单位 例如 DAYS表示天数
     * @return url
     */
    public static String getObjectUrl(String objectName, Integer duration, TimeUnit unit){
        try {
            if (null == duration || null == unit){
                duration = 7;
                unit = TimeUnit.DAYS;
            }
            GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                    .bucket(getOssProp().getBucketName())
                    .method(Method.GET)
                    .expiry(duration, unit)
                    .object(objectName)
                    .build();
            return getMinioClient().getPresignedObjectUrl(args);
        }catch (Exception e){
            log.error(e.getMessage());
            return null;
        }
    }

    /**
     * minio对象存储 获取文件外链 默认7天有效
     * @param objectName 文件名称
     * @return url
     */
    public static String getObjectUrl(String objectName){
        return getObjectUrl(objectName,7,TimeUnit.DAYS);
    }

    /**
     * minio对象存储 删除文件
     * @param objectName 文件名称 有路径需要带路径
     * @return 文件是否删除成功的信息
     */
    public static Boolean delete(String objectName){
        try {
            RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                    .bucket(getOssProp().getBucketName())
                    .object(objectName)
                    .build();
            getMinioClient().removeObject(removeObjectArgs);
            return true;
        }catch (Exception e){
            log.error(e.getMessage());
            return false;
        }
    }

    /**
     * minio对象存储 列出桶的对象列表信息
     * @param objectNamePrefix 对象名前缀
     * @param maxKeys 最大值
     * @param isSubDir 是否包含子目录
     * @return 对象列表信息
     */
    public static List<Map<String,String>> listObjects(String objectNamePrefix, Integer maxKeys, Boolean isSubDir){
        try {
            ListObjectsArgs listObjectsArgs = ListObjectsArgs.builder()
                    .bucket(getOssProp().getBucketName())
                    .maxKeys(maxKeys)
                    .prefix(objectNamePrefix)
                    .build();
            Iterable<Result<Item>> listObjects = getMinioClient().listObjects(listObjectsArgs);
            List<Map<String,String>> list = new ArrayList<>();
            listObjects.forEach(itemResult -> {
                        try {
                            Item item = itemResult.get();
                            if (item.isDir()){
                                if (isSubDir){
                                    list.addAll(Objects.requireNonNull(listObjects(item.objectName(), maxKeys, true)));
                                }
                            }else {
                                Map<String,String> map = new HashMap<>();
                                map.put("name",item.objectName());
                                map.put("url",getOssProp().getHost() + "/" + getOssProp().getBucketName() + "/" + item.objectName());
                                map.put("size", FileUtils.convertFileSize(item.size()));
                                map.put("lastModified",item.lastModified().format(DATE_TIME_FORMATTER));
                                list.add(map);
                            }
                        }catch (Exception e){
                            log.error(e.getMessage());
                        }
                    });
            return list;
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    /**
     * minio对象存储 列出桶的对象列表信息 默认最大值1000 包括子目录
     * @param objectNamePrefix 对象名前缀
     * @param isSubDir 是否包含子目录
     * @return 对象列表信息
     */
    public static List<Map<String,String>> listObjects(String objectNamePrefix, Boolean isSubDir) {
        return listObjects(objectNamePrefix, 1000, isSubDir);
    }

}
