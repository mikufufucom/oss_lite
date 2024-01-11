package org.demo.oss.storage.impl;

import org.demo.oss.storage.StorageMode;
import org.demo.oss.utils.MinioUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Minio存储模式实现类
 */
public class MinioMode implements StorageMode {
    @Override
    public String upload(MultipartFile multipartFile, String pathName, String objectName) {
        return MinioUtils.upload(multipartFile,pathName,objectName);
    }

    @Override
    public InputStream download(String objectName) {
        return MinioUtils.download(objectName);
    }

    @Override
    public Boolean delete(String fileName) {
        return MinioUtils.delete(fileName);
    }

    @Override
    public String getObjectUrl(String objectName) {
        return MinioUtils.getObjectUrl(objectName);
    }

    @Override
    public List<Map<String, String>> listObjects(String objectNamePrefix, Boolean isSubDir) {
        return MinioUtils.listObjects(objectNamePrefix,isSubDir);
    }
}
