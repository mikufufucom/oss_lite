package org.demo.oss.config;

import lombok.Getter;
import org.demo.oss.config.entity.OssProp;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里OSS 配置类
 */
@Getter
@Configuration
@EnableConfigurationProperties(OssProp.class)
public class OssConfig {

    @Autowired
    private OssProp ossProp;
    
    @Bean
    public OSS ossClient() {
        return new OSSClientBuilder().build(
                ossProp.getEndpoint(),
                ossProp.getAccessKey(),
                ossProp.getSecretKey()
        );
    }
}
