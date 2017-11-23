package com.haiercash.spring.weixin.entity;

import com.haiercash.core.lang.Convert;
import com.haiercash.spring.utils.BusinessException;
import lombok.Data;

import java.util.Objects;

/**
 * Created by 许崇雷 on 2017-11-15.
 */
@Data
public class WeiXinResponse {
    private Integer errcode;
    private String errormsg;

    public void assertSuccess() {
        if (this.errcode == null || Objects.equals(this.errcode, 0))
            return;
        throw new BusinessException("WX" + Convert.toString(this.errcode), this.errormsg);
    }
}
