package org.demo.oss.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.demo.oss.model.SysSetting;
import org.demo.oss.mapper.SysSettingMapper;
import org.demo.oss.service.SysSettingService;
@Service
public class SysSettingServiceImpl extends ServiceImpl<SysSettingMapper, SysSetting> implements SysSettingService{

    @Override
    public String getStorage() {
        SysSetting storage = getOne(new QueryWrapper<SysSetting>().lambda()
                .eq(SysSetting::getCode, "storage")
                .eq(SysSetting::getStatus, 0)

        );
        if (storage != null) {
            return storage.getValue();
        }
        return null;
    }
}
