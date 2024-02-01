package org.demo.oss.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.demo.oss.model.AjaxResult;
import org.demo.oss.model.SysSetting;
import org.demo.oss.service.impl.SysSettingServiceImpl;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;

/**
* 系统设置表(sys_setting)表控制层
*/
@RestController
@RequestMapping("/sys_setting")
public class SysSettingController {

    @Autowired
    private SysSettingServiceImpl sysSettingServiceImpl;

    @GetMapping("/get")
    public AjaxResult<SysSetting> get(String code){
        return AjaxResult.data(sysSettingServiceImpl.getOne(new LambdaQueryWrapper<SysSetting>().eq(SysSetting::getCode,code)));
    }
}
