package org.demo.oss.storage;

import lombok.extern.slf4j.Slf4j;
import org.demo.oss.utils.FileUtils;
import org.demo.oss.utils.ImageUtils;
import org.demo.oss.utils.SpringUtils;
import org.demo.oss.utils.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 本地存储模式实现类
 * @author moxiaoli
 */
@Slf4j
public class LocalMode implements StorageMode {

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private final String host = SpringUtils.getHost();

    private final String uploadPath = SpringUtils.getProperty("web.resource-path");

    @Override
    public String upload(MultipartFile multipartFile) {
        return upload(multipartFile,  multipartFile.getOriginalFilename());
    }

    @Override
    public String upload(MultipartFile multipartFile, String objectName) {
        return upload(multipartFile, "", objectName);
    }

    @Override
    public String upload(String pathName, MultipartFile multipartFile) {
        return upload(multipartFile, pathName, multipartFile.getOriginalFilename());
    }

    @Override
    public String upload(MultipartFile multipartFile, String pathName, String objectName) {
        if (null == multipartFile || 0 == multipartFile.getSize()) {
            throw new RuntimeException("文件不能为空");
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
            throw new RuntimeException("文件上传失败");
        }
    }

    @Override
    public String upload(InputStream inputStream, String pathName, String objectName) {
        try {
            String filePath = uploadPath + pathName;
            File file = new File(filePath);
            if (!file.exists()) {
                file.mkdirs();
            }
            String fileName = filePath + "/" + objectName;
            String suffix = ImageUtils.getImageSuffix(objectName);
            BufferedImage bufferedImage = ImageIO.read(inputStream);
            ImageIO.write(bufferedImage,suffix,new File(fileName));
            return host + "/" + pathName + "/" + objectName;
        } catch (Exception e) {
            log.error("文件上传失败", e);
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
        if (StringUtils.isBlank(objectName)) {
            throw new RuntimeException("文件名不能为空");
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
    public Boolean delete(String objectName) {
        if (StringUtils.isBlank(objectName)) {
            return false;
        }
        try {
            File file = new File(uploadPath + objectName);
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
            throw new RuntimeException("文件名不能为空");
        }
        return host + "/" + objectName;
    }

    @Override
    public String getObjectUrl(String objectName, Integer duration, TimeUnit unit) {
        return getObjectUrl(objectName);
    }

    @Override
    public String getObjectUrlLong(String objectName) {
        return getObjectUrl(objectName);
    }

    @Override
    public List<Map<String, String>> listObjects(String objectNamePrefix, Boolean isSubDir) {
        if (StringUtils.isBlank(objectNamePrefix)) {
            throw new RuntimeException("文件名不能为空");
        }
        try {
            // 本地文件路径
            File file = new File(uploadPath + objectNamePrefix);
            if (!file.exists()) {
                throw new RuntimeException("文件不存在");
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

    @Override
    public List<Map<String, String>> listObjects(String objectNamePrefix, Integer maxKeys, Boolean isSubDir) {
        if (StringUtils.isBlank(objectNamePrefix)) {
            throw new RuntimeException("文件名不能为空");
        }
        try {
            // 本地文件路径
            File file = new File(uploadPath + objectNamePrefix);
            if (!file.exists()) {
                throw new RuntimeException("文件不存在");
            }
            return Arrays.stream(Objects.requireNonNull(file.list())).map(fileName -> {
                Map<String, String> map = new HashMap<>();
                map.put("name", objectNamePrefix + "/" + fileName);
                map.put("url", host + "/" + objectNamePrefix + "/" + fileName);
                map.put("size", FileUtils.convertFileSize(new File(uploadPath + objectNamePrefix + "/" + fileName).length()));
                map.put("lastModified",SIMPLE_DATE_FORMAT.format(new Date(new File(uploadPath + objectNamePrefix + fileName).lastModified())));
                return map;
            }).collect(Collectors.toList()).subList(0, maxKeys);
        } catch (Exception e) {
            log.error("文件列表获取失败", e);
            throw new RuntimeException("文件列表获取失败");
        }
    }

    @Override
    public List<Map<String, String>> listObjects(String objectNamePrefix) {
        return listObjects(objectNamePrefix, false);
    }
}
