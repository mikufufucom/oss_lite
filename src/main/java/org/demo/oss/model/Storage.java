package org.demo.oss.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 存储桶表
 */
@Data
@TableName(value = "`storage`")
public class Storage implements Serializable {
    @TableId(value = "id", type = IdType.INPUT)
    private Integer id;

    /**
     * 对象存储服务商（目前支持阿里云、minio和本地）
     */
    @TableField(value = "`storage`")
    private String storage;

    /**
     * 对象存储服务的类型（1.minio，2.oss，3.local）
     */
    @TableField(value = "storage_type")
    private Integer storageType;

    /**
     * 对象存储服务商名称
     */
    @TableField(value = "storage_name")
    private String storageName;

    /**
     * 外链访问地址
     */
    @TableField(value = "`host`")
    private String host;

    /**
     * API访问地址
     */
    @TableField(value = "endpoint")
    private String endpoint;

    /**
     * 账号或者用户识别码
     */
    @TableField(value = "access_key")
    private String accessKey;

    /**
     * 密钥
     */
    @TableField(value = "secret_key")
    private String secretKey;

    /**
     * 存储桶名称
     */
    @TableField(value = "bucket_name")
    private String bucketName;

    /**
     * icon的url链接
     */
    @TableField(value = "icon")
    private String icon;

    private static final long serialVersionUID = 1L;
}