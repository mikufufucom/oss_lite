package org.demo.oss.service;

import org.demo.oss.model.SysSetting;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 *  系统设置 服务类
 */
public interface SysSettingService extends IService<SysSetting>{

    /**
     * 获取存储方式
     * @return 存储方式
     */
    String getStorage();
}
