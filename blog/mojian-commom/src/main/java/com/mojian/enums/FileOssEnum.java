package com.mojian.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: quequnlong
 * @date: 2025/2/21
 * @description:
 */
@Getter
@AllArgsConstructor
public enum FileOssEnum {
    QINIU("qiniu"),
    ALI("ali"),
    TENCENT("tencent"),
    MINIO("minio"),
    LOCAL("local");

    private final String value;
}
