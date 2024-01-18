package org.demo.oss.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.demo.oss.storage.StorageMode;
import org.demo.oss.utils.OssUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 阿里OSS存储模式实现类
 */
@Slf4j
public class OssMode implements StorageMode {

    @Override
    public String upload(MultipartFile multipartFile, String pathName, String objectName) {
        return OssUtils.upload(multipartFile,pathName,objectName);
    }

    @Override
    public String upload(InputStream inputStream, String pathName, String objectName) {
        String url = OssUtils.upload(inputStream,pathName,objectName);
        if (null != inputStream){
            try {
                inputStream.close();
            }catch (Exception e){
                log.error("文件流关闭失败：{}",e.getMessage());
            }
        }
        return url;
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
