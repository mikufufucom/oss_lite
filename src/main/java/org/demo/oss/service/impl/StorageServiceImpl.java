package org.demo.oss.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.demo.oss.service.SysSettingService;
import org.demo.oss.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.demo.oss.model.Storage;
import org.demo.oss.mapper.StorageMapper;
import org.demo.oss.service.StorageService;

import java.util.HashMap;
import java.util.Map;

@Service
public class StorageServiceImpl extends ServiceImpl<StorageMapper, Storage> implements StorageService{

    @Autowired
    private SysSettingService sysSettingService;

    /**
     * 存储类型映射表，用于将存储类型编码转换为存储类型名称
     */
    private static final Map<Integer,String> STORAGE_TYPE_MAP = new HashMap<Integer,String>(){{
        put(1,"minio");
        put(2,"oss");
        put(3,"local");
    }};

    @Override
    public Storage getStorage() {
        String storageCode = sysSettingService.getStorage();
        if(StringUtils.isBlank(storageCode)){
            throw new RuntimeException("存储服务商编码为空");
        }
        Storage storage = getOne(new LambdaQueryWrapper<Storage>()
                .eq(Storage::getStorage,storageCode)
        );
        if(storage == null){
            throw new RuntimeException("未找到存储方式");
        }
        return storage;
    }

    @Override
    public String getStorageType(String storageCode) {
        Integer storageType = getStorage().getStorageType();
        return STORAGE_TYPE_MAP.get(storageType);
    }
}
