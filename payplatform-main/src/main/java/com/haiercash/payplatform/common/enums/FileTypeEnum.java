package com.haiercash.payplatform.common.enums;

import com.bestvike.collection.ArrayUtils;
import com.bestvike.lang.StringUtils;

/**
 * Created by 许崇雷 on 2017-10-10.
 */
public enum FileTypeEnum {
    /**
     * 未知
     */
    None(StringUtils.EMPTY),
    /**
     * 申请表
     */
    ApplyTable("DOC001"),
    /**
     * 申请人身份证
     */
    ApplyIDCard("DOC002");

    private String value;

    public String getValue() {
        return this.value;
    }

    FileTypeEnum(String value) {
        this.value = value;
    }

    public static FileTypeEnum forName(String value) {
        for (FileTypeEnum fileTypeEnum : FileTypeEnum.values()) {
            if (StringUtils.equals(fileTypeEnum.value, value))
                return fileTypeEnum;
        }
        return None;
    }

    public boolean isIn(FileTypeEnum... fileTypeEnums) {
        return ArrayUtils.contains(fileTypeEnums, this);
    }
}
