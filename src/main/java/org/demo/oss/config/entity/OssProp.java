package org.demo.oss.config.entity;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 对象存储配置类
 */
@Data
@ConfigurationProperties(prefix = "upload")
public class OssProp {

    /**
     * 对象存储服务商，目前支持阿里云、minio
     */
    private String storage;
    /**
     * minio外链地址
     */
    private String host;
    /**
     * minio服务的API地址
     */
    private String endpoint;
    /**
     * minio的accessKey
     */
    private String accessKey;
    /**
     * minio的secretKey
     */
    private String secretKey;
    /**
     * minio的存储桶名称
     */
    private String bucketName;
}
