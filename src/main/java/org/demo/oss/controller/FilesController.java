package org.demo.oss.controller;

import org.demo.oss.model.AjaxResult;
import org.demo.oss.service.UploadService;
import org.demo.oss.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 文件接口
 */
@Slf4j
@RestController
@RequestMapping("/files")
public class FilesController {

    @Autowired
    private UploadService uploadService;

    /**
     * 上传文件
     * @param file 文件
     * @param pathName 文件路径
     * @return 文件路径
     */
    @PostMapping("/upload")
    public AjaxResult<Map<String,String>> upload(@RequestPart("file") MultipartFile file, String pathName){
        // 如果文件为空，则返回错误信息
        if (file.isEmpty()){
            return AjaxResult.error("文件不能为空");
        }
        // 如果文件路径为空，则使用默认路径
        if (StringUtils.isBlank(pathName)){
            return AjaxResult.data(uploadService.upload(file));
        }
        return AjaxResult.data(uploadService.upload(file,pathName));
    }

    /**
     * 删除文件
     * @param fileName 文件名
     * @return 删除成功的标志
     */
    @DeleteMapping("/delete")
    public AjaxResult<Boolean> delete(String fileName){
        return AjaxResult.status(uploadService.delete(fileName),"文件删除成功","文件删除失败");
    }

    /**
     * 下载文件
     * @param fileName 文件名
     * @return 文件资源链接
     */
    @GetMapping("/download")
    public AjaxResult<String> download(String fileName){
        return AjaxResult.data(uploadService.download(fileName));
    }

    /**
     * 图片下载二进制流
     * @param fileName 文件名
     */
    @GetMapping("/downloadImage")
    public void downloadImage(String fileName, HttpServletResponse response){
        try {
            if (StringUtils.isBlank(fileName)){
                throw new RuntimeException("文件名不能为空");
            }
            uploadService.downloadImage(fileName,response.getOutputStream());
        }catch (RuntimeException | IOException e){
            log.error("文件下载失败",e);
        }
    }

    /**
     * 获取路径下的文件列表 不包含子文件夹
     * @param path 文件目录
     * @return 文件列表
     */
    @GetMapping("/listNotSubDir")
    public AjaxResult<List<Map<String,String>>> listNotSubDir(String path){
        return AjaxResult.data(uploadService.listObjects(path,false));
    }

    /**
     * 获取路径下的文件列表 包含子文件夹
     * @param path 文件目录
     * @return 文件列表
     */
    @GetMapping("/listAndSubDir")
    public AjaxResult<List<Map<String,String>>> listAndSubDir(String path){
        return AjaxResult.data(uploadService.listObjects(path,true));
    }
}
