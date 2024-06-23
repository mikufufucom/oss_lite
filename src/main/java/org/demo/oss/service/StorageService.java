package org.demo.oss.service;

import org.demo.oss.model.Storage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 *  存储方式 服务类
 */
public interface StorageService extends IService<Storage>{

    /**
     * 获取默认存储服务
     * @return 返回默认存储服务
     */
    Storage getStorage();

    /**
     * 根据存储服务编码获取对应的存储类型名称
     * @param storageCode 存储服务编码
     * @return 存储类型名称
     */
    String getStorageType(String storageCode);

}
