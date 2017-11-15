package com.haiercash.spring.weixin.entity;

import com.haiercash.core.lang.Convert;
import com.haiercash.core.lang.ObjectUtils;
import lombok.Data;

/**
 * Created by 许崇雷 on 2017-11-15.
 */
@Data
public class WeiXinResponse {
    private Integer errcode;
    private String errormsg;

    public String getErrorcodeStr() {
        return Convert.toString(this.errcode);
    }

    public boolean isSuccess() {
        return ObjectUtils.equals(this.errcode, 0);
    }
}
