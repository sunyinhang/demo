package com.haiercash.payplatform.common.entity;

import lombok.Data;

@Data
public class LoanTypeProperty {
    private String MTD_SEQ;//  标识还款方式顺序号	MTD_SEQ
    private String MTD_CDE;//  还款方式	MTD_CDE
    private String MTD_TYP;//  还款方式种类，包括等额本息、等额本金、利随本清等	MTD_TYP
    private String TNR_OPT;//  期限设定	TNR_OPT
    private String SPECIAL_IND;//  是否是阶段利率	SPECIAL_IND
    private String INT_RAT;//  客户利率	INT_RAT
    private String FIXED_OD_IND;//  是否采用固定罚息利率	FIXED_OD_IND
    private String OD_INT_RATE;//  罚息利率	OD_INT_RATE
}
