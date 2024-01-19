package org.demo.oss.service.impl;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.demo.oss.model.Storage;
import org.demo.oss.mapper.StorageMapper;
import org.demo.oss.service.StorageService;
@Service
public class StorageServiceImpl extends ServiceImpl<StorageMapper, Storage> implements StorageService{

}
