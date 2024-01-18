package org.demo.oss.utils;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 图片处理工具类
 * @author moxiaoli
 */
@Slf4j
public class ImageUtils {

    private ImageUtils(){}

    private static List<String> imageSuffixList = new ArrayList<>();

    static {
        imageSuffixList.add(".jpg");
        imageSuffixList.add(".jpeg");
        imageSuffixList.add(".png");
    }

    /**
     * 获取图片的后缀名
     * @param fileName 文件名
     * @return 图片后缀名
     */
    public static String getImageSuffix(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            throw new RuntimeException("文件名不能为空");
        }
//        // 获取文件名的最后一个点的位置
//        int index = fileName.lastIndexOf(".");
//        // 获取文件的后缀名
//        return fileName.substring(index);
        return fileName.split("\\.")[1];
    }

    /**
     * 判断是否为图片
     * @param multipartFile 文件
     * @return 是否为图片
     */
    public static boolean isImage(MultipartFile multipartFile) {
        if (null == multipartFile || 0 == multipartFile.getSize()) {
            throw new RuntimeException("文件不能为空");
        }
        String fileName = multipartFile.getOriginalFilename();
        if (StringUtils.isBlank(fileName)) {
            throw new RuntimeException("文件名不能为空");
        }
        // 获取文件的后缀名
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        String contentType = multipartFile.getContentType();
        return imageSuffixList.contains(suffix.toLowerCase()) && !StringUtils.isBlank(contentType) && contentType.startsWith("image");
    }

    /**
     * 压缩图片
     * @param multipartFile 文件
     * @param width 宽度
     * @param height 高度
     * @return 压缩后的图片
     */
    public static BufferedImage compressImage(MultipartFile multipartFile, int width, int height) {
        if (null == multipartFile || 0 == multipartFile.getSize()) {
            throw new RuntimeException("文件不能为空");
        }
        try {
            // 创建临时文件
            File file = File.createTempFile("temp", null);
            // 将文件转换成临时文件
            multipartFile.transferTo(file);
            return Thumbnails.of(file)
                    // 指定缩略图的大小
                    .size(width, height)
                    // 指定缩略图的存储路径
                    .asBufferedImage();
        } catch (IOException e) {
            log.error("图片压缩失败：{}",e.getMessage());
            throw new RuntimeException("图片压缩失败");
        }
    }

    /**
     * 压缩图片
     * @param multipartFile 文件
     * @param width 宽度
     * @param height 高度
     * @return 压缩后的图片
     */
    public static File compressImageToFile(MultipartFile multipartFile, int width, int height,String path, String fileName) {
        if (null == multipartFile || 0 == multipartFile.getSize()) {
            throw new RuntimeException("文件不能为空");
        }
        try {
            if (!isImage(multipartFile)) {
                throw new RuntimeException("文件不是图片");
            }
            if (new File("D:\\temp\\").mkdirs()) {
                log.info("创建临时文件夹成功");
            }
            // 创建临时文件
            File dir = new File("D:\\temp\\");
            if (dir.exists() && dir.isDirectory()){
                dir.mkdirs();
            }
            File file = new File("D:\\temp\\" + multipartFile.getOriginalFilename());
            // 将文件转换成临时文件
            multipartFile.transferTo(file);
            Thumbnails.of(file)
                    // 指定缩略图的大小
                    .size(width, height)
                    // 指定缩略图的存储路径
                    .toFile(path + fileName);
            return new File(path + fileName);
        } catch (IOException e) {
            log.error("图片压缩失败：{}",e.getMessage());
            throw new RuntimeException("图片压缩失败");
        }
    }

    /**
     * 压缩图片
     * @param inputStream 文件
     * @param width 宽度
     * @param height 高度
     * @return 压缩后的图片
     */
    public static InputStream compressImageToInputStream(InputStream inputStream, int width, int height,String path, String fileName) {
        Path outputPath = Paths.get(path, fileName);
        try (OutputStream outputStream = Files.newOutputStream(outputPath)) {
            Thumbnails.of(inputStream)
                    .size(width, height)
                    .toOutputStream(outputStream);
        } catch (IOException e) {
            log.error("图片压缩失败：{}", e.getMessage());
            throw new RuntimeException("图片压缩失败");
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                log.error("文件流关闭失败：{}", e.getMessage());
            }
        }
        try {
            return Files.newInputStream(outputPath);
        } catch (IOException e) {
            log.error("无法打开压缩后的图片文件：{}", e.getMessage());
            throw new RuntimeException("无法打开压缩后的图片文件");
        }
    }

    /**
     * 压缩图片
     * @param multipartFile 文件
     * @param width 宽度
     * @param height 高度
     * @return 压缩后的图片
     */
    public static InputStream compressImageToInputStream(MultipartFile multipartFile, int width, int height) {
        if (null == multipartFile || 0 == multipartFile.getSize()) {
            throw new RuntimeException("文件不能为空");
        }
        ByteArrayOutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            if (!isImage(multipartFile)) {
                throw new RuntimeException("文件不是图片");
            }
            outputStream = new ByteArrayOutputStream();
            inputStream = multipartFile.getInputStream();
            Thumbnails.of(inputStream)
                    // 指定缩略图的大小
                    .size(width, height)
                    // 指定缩略图的存储路径
                    .toOutputStream(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException e) {
            log.error("图片压缩失败：{}",e.getMessage());
            throw new RuntimeException("图片压缩失败");
        }finally {
            if (null != inputStream){
                try {
                    inputStream.close();
                }catch (Exception e){
                    log.error("文件流关闭失败：{}",e.getMessage());
                }
            }
            if (null != outputStream){
                try {
                    outputStream.close();
                }catch (Exception e){
                    log.error("文件流关闭失败：{}",e.getMessage());
                }
            }
        }
    }
}
