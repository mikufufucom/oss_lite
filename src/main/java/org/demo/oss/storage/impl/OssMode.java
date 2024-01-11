package org.demo.oss.storage.impl;

import org.demo.oss.storage.StorageMode;
import org.demo.oss.utils.OssUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 阿里OSS存储模式实现类
 */
public class OssMode implements StorageMode {

    @Override
    public String upload(MultipartFile multipartFile, String pathName, String objectName) {
        return OssUtils.upload(multipartFile,pathName,objectName);
    }

    @Override
    public InputStream download(String objectName) {
        return OssUtils.download(objectName);
    }

    @Override
    public Boolean delete(String fileName) {
        return OssUtils.delete(fileName);
    }

    @Override
    public String getObjectUrl(String objectName) {
        return OssUtils.getObjectUrl(objectName);
    }
    @Override
    public List<Map<String, String>> listObjects(String objectNamePrefix, Boolean isSubDir) {
        return OssUtils.listObjects(objectNamePrefix);
    }
}
