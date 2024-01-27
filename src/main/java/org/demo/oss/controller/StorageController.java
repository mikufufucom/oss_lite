package org.demo.oss.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.demo.oss.model.AjaxResult;
import org.demo.oss.model.Storage;
import org.demo.oss.model.SysSetting;
import org.demo.oss.service.impl.StorageServiceImpl;
import org.demo.oss.service.impl.SysSettingServiceImpl;
import org.demo.oss.utils.StringUtils;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
* 存储桶表(storage)表控制层
*/
@RestController
@RequestMapping("/storage")
public class StorageController {

    @Autowired
    private StorageServiceImpl storageServiceImpl;
    @Autowired
    private SysSettingServiceImpl sysSettingServiceImpl;

    /**
     * 获取存储服务商
     * @return 存储服务商
     */
    @GetMapping("/getStorageSetting")
    public AjaxResult<Storage> storage(){
        LambdaQueryWrapper<SysSetting> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysSetting::getCode,"storage");
        String storage = sysSettingServiceImpl.getOne(wrapper).getValue();
        return AjaxResult.data(storageServiceImpl.getOne(new LambdaQueryWrapper<Storage>().eq(Storage::getStorage,storage)));
    }

    /**
     * 获取存储服务商信息
     * @return 存储服务商信息
     */
    @GetMapping("/getStorageInfo")
    public AjaxResult<Storage> storageInfo(String storage){
        if (StringUtils.isBlank(storage)){
            return AjaxResult.error("存储服务商不能为空");
        }
        return AjaxResult.data(storageServiceImpl.getOne(new LambdaQueryWrapper<Storage>().eq(Storage::getStorage,storage)));
    }

    @GetMapping("/list")
    public AjaxResult<List<Storage>> list(){
        return AjaxResult.data(storageServiceImpl.list());
    }

    /**
     * 设置存储服务商
     * @return 存储服务商
     */
    @PostMapping("/updateStorageSetting")
    public AjaxResult<String> storage(@RequestBody Storage storage){
        boolean flag = false;
        if (storage!=null){
            if(StringUtils.isBlank(storage.getStorage())){
                return AjaxResult.error("存储服务商不能为空");
            }
            LambdaUpdateWrapper<SysSetting> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(SysSetting::getCode,"storage");
            wrapper.set(SysSetting::getValue,storage.getStorage());
            flag = sysSettingServiceImpl.update(wrapper);
        }
        if (flag){
            return AjaxResult.status(storageServiceImpl.update(storage,new LambdaUpdateWrapper<Storage>().eq(Storage::getStorage,storage.getStorage())),"存储服务商修改成功","存储服务商修改失败");
        }else {
            return AjaxResult.error("存储服务商修改失败");
        }
    }

}
