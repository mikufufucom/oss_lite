package org.demo.oss.config;

import lombok.Getter;
import org.demo.oss.config.entity.OssProp;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * minIO 配置
 */
@Getter
@Configuration
@EnableConfigurationProperties(OssProp.class)
public class MinioConfig {
    @Autowired
    private OssProp ossProp;

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
        return minioClient();
    }
}
