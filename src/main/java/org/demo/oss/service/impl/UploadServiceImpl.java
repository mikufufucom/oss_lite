package org.demo.oss.service.impl;

import org.demo.oss.config.enums.StorageType;
import org.demo.oss.config.enums.UploadFileType;
import org.demo.oss.service.SysSettingService;
import org.demo.oss.service.UploadService;
import lombok.extern.slf4j.Slf4j;
import org.demo.oss.utils.ImageUtils;
import org.demo.oss.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 文件处理接口实现类
 */
@Slf4j
@Service
public class UploadServiceImpl implements UploadService {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    @Autowired
    private SysSettingService settingService;

    private String getStorage(){
        String storage = settingService.getStorage();
        if (!StringUtils.isBlank(storage)) {
            log.info("当前存储服务商为：{}",storage);
            return storage;
        }
        return "local";
    }

    @Override
    public Map<String,String> upload(MultipartFile multipartFile) {
        // 将上传文件的类型转换成全大写
        String type = Objects.requireNonNull(multipartFile.getContentType()).split("/")[0].toUpperCase();
        return upload(multipartFile, UploadFileType.valueOf(type).getPath());
    }

    @Override
    public Map<String,String> upload(MultipartFile multipartFile, String pathName) {
        String dateString = simpleDateFormat.format(System.currentTimeMillis());
        String fileName = dateString + "_" + multipartFile.getOriginalFilename();
//        return StorageType.getStorageMode(getStorage()).upload(multipartFile,pathName,fileName);
        InputStream inputStream = null;
        try {
            inputStream = multipartFile.getInputStream();
            String url = StorageType.getStorageMode(getStorage()).upload(multipartFile,pathName,fileName);
            String thumbUrl = StorageType.getStorageMode(getStorage()).upload(ImageUtils.compressImageToInputStream(multipartFile,200,200),"thumb","thumb_" + fileName);
            return new HashMap<String,String>(){{
                put("url",url);
                put("thumbUrl",thumbUrl);
            }};
        }catch (IOException io){
            log.error(io.getMessage());
        }finally {
            if (null != inputStream){
                try {
                    inputStream.close();
                }catch (Exception e){
                    log.error("文件流关闭失败：{}",e.getMessage());
                }
            }
        }
        return null;
    }

    @Override
    public String download(String fileName) {
        return StorageType.getStorageMode(getStorage()).getObjectUrl(fileName);
    }

    @Override
    public Boolean delete(String fileName) {
        return StorageType.getStorageMode(getStorage()).delete(fileName);
    }

    @Override
    public void downloadImage(String fileName, OutputStream outputStream) {
        InputStream stream = null;
        try {
            stream = StorageType.getStorageMode(getStorage()).download(fileName);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        try {
            if (stream == null) {
                throw new IOException("文件下载失败");
            }
            BufferedImage bufferedImage = ImageIO.read(stream);
            String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
            ImageIO.write(bufferedImage, suffix, outputStream);
        }catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public List<Map<String,String>> listObjects(String objectNamePrefix, Boolean isSubDir) {
        return StorageType.getStorageMode(getStorage()).listObjects(objectNamePrefix,isSubDir);
    }
}
