package org.demo.oss.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.demo.oss.config.entity.OssProp;
import org.demo.oss.storage.StorageMode;
import org.demo.oss.utils.FileUtils;
import org.demo.oss.utils.SpringUtils;
import org.demo.oss.utils.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 本地存储模式实现类
 */
@Slf4j
public class LocalMode implements StorageMode {

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private final String host = SpringUtils.getBean(OssProp.class).getHost();

    private final String uploadPath = SpringUtils.getProperty("web.resource-path");

    @Override
    public String upload(MultipartFile multipartFile, String pathName, String objectName) {
        if (null == multipartFile || 0 == multipartFile.getSize()) {
            return null;
        }
        try {
            String filePath = uploadPath + pathName;
            File file = new File(filePath);
            if (!file.exists()) {
                file.mkdirs();
            }
            String path = filePath + "/" + objectName;
            multipartFile.transferTo(new File(path));
            return host + "/" + pathName + "/" + objectName;
        } catch (Exception e) {
            log.error("文件上传失败", e);
        }
        return null;
    }

    @Override
    public InputStream download(String objectName) {
        if (StringUtils.isBlank(objectName)) {
            return null;
        }
        try {
            // 本地文件路径
            return new File(uploadPath + objectName).toURI().toURL().openStream();
        } catch (Exception e) {
            log.error("文件下载失败", e);
            throw new RuntimeException("文件下载失败");
        }
    }

    @Override
    public Boolean delete(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return false;
        }
        try {
            File file = new File(uploadPath + fileName);
            if (file.exists()) {
                return file.delete();
            }
            return false;
        } catch (Exception e) {
            log.error("文件删除失败", e);
            throw new RuntimeException("文件删除失败");
        }
    }

    @Override
    public String getObjectUrl(String objectName) {
        if (StringUtils.isBlank(objectName)) {
            return null;
        }
        return host + "/" + objectName;
    }

    @Override
    public List<Map<String, String>> listObjects(String objectNamePrefix, Boolean isSubDir) {
        if (StringUtils.isBlank(objectNamePrefix)) {
            return null;
        }
        try {
            // 本地文件路径
            File file = new File(uploadPath + objectNamePrefix);
            if (!file.exists()) {
                return null;
            }
            return Arrays.stream(Objects.requireNonNull(file.list())).map(fileName -> {
                Map<String, String> map = new HashMap<>();
                map.put("name", objectNamePrefix + "/" + fileName);
                map.put("url", host + "/" + objectNamePrefix + "/" + fileName);
                map.put("size", FileUtils.convertFileSize(new File(uploadPath + objectNamePrefix + "/" + fileName).length()));
                map.put("lastModified",SIMPLE_DATE_FORMAT.format(new Date(new File(uploadPath + objectNamePrefix + fileName).lastModified())));
                return map;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("文件列表获取失败", e);
            throw new RuntimeException("文件列表获取失败");
        }
    }
}
