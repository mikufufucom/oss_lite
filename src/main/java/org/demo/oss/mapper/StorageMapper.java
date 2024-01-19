package org.demo.oss.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.demo.oss.model.Storage;

@Mapper
public interface StorageMapper extends BaseMapper<Storage> {
}