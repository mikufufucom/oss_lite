package org.demo.oss.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.minio.MinioClient;
import lombok.Getter;
import org.demo.oss.config.entity.OssProp;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.demo.oss.model.Storage;
import org.demo.oss.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 通用OSS 配置类
 */
@Getter
@Configuration
@EnableConfigurationProperties(OssProp.class)
public class OssConfig {

    @Autowired
    private OssProp ossProp;
    @Autowired
    private StorageService storageService;
    
    @Bean
    public OSS ossClient() {
        return new OSSClientBuilder().build(
                ossProp.getEndpoint(),
                ossProp.getAccessKey(),
                ossProp.getSecretKey()
        );
    }

    /**
     * 获取配置的Minio客户端
     * @return Minio客户端
     */
    @Bean
    public MinioClient minioClient(){
        return MinioClient.builder()
                .endpoint(ossProp.getEndpoint())
                .credentials(ossProp.getAccessKey(), ossProp.getSecretKey())
                .build();
    }

    /**
     * 获取配置的Minio客户端
     * @return Minio客户端
     */
    public MinioClient getMinioClient(){
        Storage storage = storageService.getOne(new QueryWrapper<Storage>().lambda().eq(Storage::getStorage, "minio"));
        if (storage != null) {
            return MinioClient.builder()
                    .endpoint(storage.getEndpoint())
                    .credentials(storage.getAccessKey(), storage.getSecretKey())
                    .build();
        }
        return minioClient();
    }

    /**
     * 获取配置的阿里OSS客户端
     * @return 阿里OSS客户端
     */
    public OSS getOssClient(){
        Storage storage = storageService.getOne(new QueryWrapper<Storage>().lambda().eq(Storage::getStorage, "oss"));
        if (storage != null) {
            return new OSSClientBuilder().build(
                    storage.getEndpoint(),
                    storage.getAccessKey(),
                    storage.getSecretKey()
            );
        }
        return ossClient();
    }
}
