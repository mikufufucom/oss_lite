package org.demo.oss.controller;
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

}
