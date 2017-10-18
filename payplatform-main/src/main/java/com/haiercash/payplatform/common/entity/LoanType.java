package com.haiercash.payplatform.common.entity;

import com.bestvike.lang.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by 许崇雷 on 2017-10-18.
 */
@Getter
@Setter
@ToString
public class LoanType {
    private String typCde;//贷款品种
    private String typSeq;//标识贷款品种顺序号
    private String typGrp;//贷款类型
    private String minAmt;//单笔最小贷款金额
    private String maxAmt;//单笔最大贷款金额
    private String tnrOpt;//贷款期限
    private String tnrMaxDays;//最大天数
    private String mtdCde;//还款方式代码
    private String mtdDesc;//还款方式描述
    private String typLvlCde;//如果存在，即提供 贷款品种所属小类代码		否	如果存在，即提供
    private String typLvlDesc;//如果存在，即提供 贷款品种所属小类名称			否	如果存在，即提供

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LoanType))
            return false;
        LoanType that = ((LoanType) o);
        return StringUtils.equals(this.typCde, that.typCde)
                && StringUtils.equals(this.typGrp, that.typGrp)
                && StringUtils.equals(this.tnrOpt, that.tnrOpt)
                && StringUtils.equals(this.mtdCde, that.mtdCde);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.typCde == null ? 0 : this.typCde.hashCode());
        result = prime * result + (this.typGrp == null ? 0 : this.typGrp.hashCode());
        result = prime * result + (this.tnrOpt == null ? 0 : this.tnrOpt.hashCode());
        result = prime * result + (this.mtdCde == null ? 0 : this.mtdCde.hashCode());
        return result;
    }
}
