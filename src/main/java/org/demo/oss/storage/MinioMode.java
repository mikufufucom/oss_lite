package org.demo.oss.storage;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.demo.oss.model.Storage;
import org.demo.oss.service.StorageService;
import org.demo.oss.utils.FileUtils;
import org.demo.oss.utils.SpringUtils;
import org.demo.oss.utils.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Minio存储模式实现类
 * @author moxiaoli
 */
@Slf4j
public class MinioMode implements StorageMode {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 获取配置的Minio客户端
     * @return Minio客户端
     */
    public MinioClient getMinioClient(){
        Storage storage = getOssProp();
        if (storage != null) {
            return MinioClient.builder()
                    .endpoint(storage.getEndpoint())
                    .credentials(storage.getAccessKey(), storage.getSecretKey())
                    .build();
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
        log.info("打印文件资源名{}",multipartFile.getOriginalFilename());
        return upload(multipartFile,multipartFile.getOriginalFilename());
    }

    @Override
    public String upload(MultipartFile multipartFile, String objectName) {
        // 判断上传文件是否为空
        if (null == multipartFile || 0 == multipartFile.getSize()) {
            throw new RuntimeException("文件不能为空");
        }
        return upload(multipartFile,"",objectName);
    }

    @Override
    public String upload(String pathName, MultipartFile multipartFile) {
        // 判断上传文件是否为空
        if (null == multipartFile || 0 == multipartFile.getSize()) {
            throw new RuntimeException("文件不能为空");
        }
        return upload(multipartFile,pathName,multipartFile.getOriginalFilename());
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
            //minio的上传文件参数
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    //设置数据桶
                    .bucket(getOssProp().getBucketName())
                    //上传的文件名
                    .object(pathName + "/" + objectName)
                    //上传的文件类型
                    .contentType(multipartFile.getContentType())
                    //上传的文件二进制流
                    .stream(inputStream, multipartFile.getSize(), -1)
                    .build();
            //调用MinioClient的putObject方法上传文件
            getMinioClient().putObject(putObjectArgs);
            if (StringUtils.isBlank(pathName)) {
                return getOssProp().getHost() + "/" + getOssProp().getBucketName() + "/" + objectName;
            }
            return getOssProp().getHost() + "/" + getOssProp().getBucketName() + "/" + pathName + "/" + objectName;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException("上传文件失败");
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
            throw new RuntimeException("文件上传失败");
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
    public InputStream download(String objectName) {
        try {
            //minio的获取文件的参数
            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(getOssProp().getBucketName())
                    .object(objectName)
                    .build();
            //调用MinioClient的getObject方法获取文件
            return getMinioClient().getObject(getObjectArgs);
        }catch (Exception e){
            log.error(e.getMessage());
            throw new RuntimeException("文件下载失败");
        }
    }

    @Override
    public Boolean delete(String objectName) {
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

    @Override
    public String getObjectUrl(String objectName) {
        return getObjectUrl(objectName,7,TimeUnit.DAYS);
    }

    @Override
    public String getObjectUrl(String objectName, Integer duration, TimeUnit unit) {
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
            throw new RuntimeException("获取文件链接失败");
        }
    }

    @Override
    public String getObjectUrlLong(String objectName) {
        return getObjectUrl(objectName,0,null);
    }

    @Override
    public List<Map<String, String>> listObjects(String objectNamePrefix, Boolean isSubDir) {
        return listObjects(objectNamePrefix, 1000, isSubDir);
    }

    @Override
    public List<Map<String, String>> listObjects(String objectNamePrefix, Integer maxKeys, Boolean isSubDir) {
        try {
            ListObjectsArgs listObjectsArgs = ListObjectsArgs.builder()
                    .bucket(getOssProp().getBucketName())
                    //设置取出数量的最大值
                    .maxKeys(maxKeys)
                    //设置前缀
                    .prefix(objectNamePrefix)
                    .build();
            Iterable<Result<Item>> listObjects = getMinioClient().listObjects(listObjectsArgs);
            List<Map<String,String>> list = new ArrayList<>();
            listObjects.forEach(itemResult -> {
                try {
                    Item item = itemResult.get();
                    // 当这个对象是文件夹时
                    if (item.isDir()){
                        // 如果包含子文件夹的对象
                        if (isSubDir){
                            list.addAll(Objects.requireNonNull(listObjects(item.objectName(), maxKeys, isSubDir)));
                        }
                    }else {
                        Map<String,String> map = new HashMap<>();
                        map.put("name",item.objectName());
                        map.put("url",getOssProp().getHost() + "/" + getOssProp().getBucketName() + "/" + item.objectName());
                        map.put("size", FileUtils.convertFileSize(item.size()));
                        map.put("lastModified",item.lastModified().format(DATE_TIME_FORMATTER));
                        list.add(map);
                    }
                    // 写法2
                    // 当这个对象不是文件夹时
//                            if (!item.isDir()){
//                                String url = getOssProp().getHost() + "/" + getOssProp().getBucketName() + "/" + item.objectName();
//                                list.add(url);
//                                // 如果包含子文件夹的对象
//                            }else if (isSubDir){
//                                list.addAll(listObjects(item.objectName(), maxKeys, isSubDir));
//                            }
                }catch (Exception e){
                    log.error(e.getMessage());
                }
            });
            return list;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException("获取存储对象列表失败");
        }
    }

    @Override
    public List<Map<String, String>> listObjects(String objectNamePrefix) {
        try {
            ListObjectsArgs listObjectsArgs = ListObjectsArgs.builder()
                    .bucket(getOssProp().getBucketName())
                    //设置前缀
                    .prefix(objectNamePrefix)
                    .build();
            Iterable<Result<Item>> listObjects = getMinioClient().listObjects(listObjectsArgs);
            List<Map<String,String>> list = new ArrayList<>();
            listObjects.forEach(itemResult -> {
                try {
                    Item item = itemResult.get();
                    // 当这个对象是文件夹时
                    if (item.isDir()){
                        list.addAll(Objects.requireNonNull(listObjects(item.objectName())));
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
            throw new RuntimeException("获取存储对象列表失败");
        }
    }
}
