package org.demo.oss.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.demo.oss.service.StorageService;
import org.demo.oss.service.SysSettingService;
import org.demo.oss.service.UploadService;
import org.demo.oss.storage.StorageMode;
import org.demo.oss.storage.enums.StorageType;
import org.demo.oss.storage.enums.UploadFileType;
import org.demo.oss.utils.ImageUtils;
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
 * @author moxiaoli
 */
@Slf4j
@Service
public class UploadServiceImpl implements UploadService {

    // SimpleDateFormat是处理日期格式的类
    private static final  SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @Autowired
    private SysSettingService settingService;
    @Autowired
    private StorageService storageService;

    /**
     * 存储模式
     */
    private volatile StorageMode storageMode = null;

    /**
     * 获取存储类型。该方法首先从设置服务中获取存储编码，然后基于存储编码查询存储类型。
     * 如果存储编码存在但对应的存储类型不存在，则记录错误日志并抛出运行时异常。
     * 如果存储编码不存在，则默认返回"local"作为存储类型。
     *
     * @return 返回存储类型字符串。如果无法确定存储类型，则抛出运行时异常。
     */
    private String getStorageType(){
        // 从设置服务获取存储编码
        String storageCode = settingService.getStorage();
        if (StringUtils.isNotBlank(storageCode)) {
            // 记录存储编码信息
            log.info("当前存储服务编码为：{}",storageCode);
            // 根据存储编码获取存储类型
            String storageType = storageService.getStorageType(storageCode);
            // 记录存储类型信息
            log.info("当前存储服务类型为：{}",storageType);
            // 如果存储类型为空，记录错误日志
            if (StringUtils.isBlank(storageType)) {
                log.error("当前存储服务商暂时不支持");
                // 并抛出运行时异常
                throw new RuntimeException("当前存储服务商暂时不支持");
            }
            // 返回获取到的存储类型
            return storageType;
        }
        // 如果没有获取到存储编码，返回默认的存储类型"local"
        return "local";
    }

    /**
     * 初始化存储模式。如果存储模式尚未被初始化，则调用此方法。
     * 由于使用了volatile关键字，此方法确保了线程之间的可见性。
     */
    private void initStorageMode() {
        if (storageMode == null) {
            synchronized (this) {
                // 再次检查以避免双重检查锁定(DCL)的问题
                if (storageMode == null) {
                    storageMode = StorageType.getStorageMode(getStorageType());
                }
            }
        }
    }

    /**
     * 更新存储模式。
     * 该方法会检查当前的存储模式是否已经设置。如果已经设置，会根据获取的存储类型来更新存储模式；
     * 如果未设置，则会调用initStorageMode方法来初始化存储模式。
     *
     * @return 总是返回true，表示存储模式已成功更新或初始化。
     */
    private Boolean updateStorageMode() {
        if (storageMode != null){
            // 存储模式已设置，根据当前的存储类型更新存储模式
            storageMode = StorageType.getStorageMode(getStorageType());
        }else {
            // 存储模式未设置，进行初始化
            initStorageMode();
        }
        return true;
    }

    @Override
    public Map<String,String> upload(MultipartFile multipartFile) {
        // 将上传文件的类型转换成全大写
        String type = Objects.requireNonNull(multipartFile.getContentType()).split("/")[0].toUpperCase();
        return upload(multipartFile, UploadFileType.valueOf(type).getPath());
    }

    @Override
    public Map<String,String> upload(MultipartFile multipartFile, String pathName) {
        // 初始化存储方式
        initStorageMode();
        String dateString = SIMPLE_DATE_FORMAT.format(System.currentTimeMillis());
        String fileName = dateString + "_" + multipartFile.getOriginalFilename();
//        return storageMode.upload(multipartFile,pathName,fileName);
        InputStream inputStream = null;
        try {
            inputStream = multipartFile.getInputStream();
            String url = storageMode.upload(multipartFile,pathName,fileName);
            String thumbUrl = storageMode.upload(ImageUtils.compressImageToInputStream(multipartFile,200,200),"thumb","thumb_" + fileName);
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
        initStorageMode();
        return storageMode.getObjectUrl(fileName);
    }

    @Override
    public Boolean delete(String fileName) {
        initStorageMode();
        return storageMode.delete(fileName);
    }

    @Override
    public void downloadImage(String fileName, OutputStream outputStream) {
        initStorageMode();
        InputStream stream = null;
        try {
            stream = storageMode.download(fileName);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        try {
            if (stream == null) {
                throw new IOException("文件下载失败");
            }
            BufferedImage bufferedImage = ImageIO.read(stream);
            String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
//            ImageIO.write(bufferedImage, "jpg", outputStream);
            ImageIO.write(bufferedImage, suffix, outputStream);
        }catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public List<Map<String,String>> listObjects(String objectNamePrefix, Boolean isSubDir) {
        initStorageMode();
        return storageMode.listObjects(objectNamePrefix,isSubDir);
    }

    @Override
    public List<Map<String,String>> list(String objectNamePrefix) {
        initStorageMode();
        return storageMode.listObjects(objectNamePrefix);
    }
}
