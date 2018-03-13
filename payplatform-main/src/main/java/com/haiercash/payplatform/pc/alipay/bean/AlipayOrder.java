package com.haiercash.payplatform.pc.alipay.bean;

import com.haiercash.core.lang.StringUtils;
import com.haiercash.spring.util.BusinessException;
import com.haiercash.spring.util.ConstUtil;
import lombok.Data;

import java.util.Date;

/**
 * Created by 许崇雷 on 2018-03-08.
 */
@Data
public class AlipayOrder {
    private String applSeq;
    private String payNo;
    private String repaySeq;
    private String repayAmt;
    private String subject;
    private Date timeoutExpire;

    public void valid() {
        if (StringUtils.isEmpty(this.applSeq)
                || StringUtils.isEmpty(this.payNo)
                || StringUtils.isEmpty(this.repaySeq)
                || StringUtils.isEmpty(this.repayAmt)
                || StringUtils.isEmpty(this.subject)
                || this.timeoutExpire == null)
            throw new BusinessException(ConstUtil.ERROR_CODE, "操作失败，刷新账单列表后重试");
    }
}
