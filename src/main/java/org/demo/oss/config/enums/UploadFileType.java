package org.demo.oss.config.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文件类型枚举类
 */

@Getter
@AllArgsConstructor
public enum UploadFileType {
    // 图片
    IMAGE("image"),
    // 视频
    VIDEO("video"),
    // 音频
    AUDIO("music"),
    // 文件
    FILE("file");

    private final String path;
}
