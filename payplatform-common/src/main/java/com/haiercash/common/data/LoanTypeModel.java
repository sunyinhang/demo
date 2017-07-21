package com.haiercash.common.data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by yinjun on 2016/7/25.
 */
@Entity
@Table(name = "LOAN_TYPE_MODEL")
public class LoanTypeModel {
    @Id
    private String id;//主键
    private String modelNo;//模板编号
    private String modelName;//模板名称（暂不用）
    //private String loanType;//贷款品种编号
    private String beiZhu;//备注
    private String typLevelTwo;

    public String getTypLevelTwo() {
        return typLevelTwo;
    }

    public void setTypLevelTwo(String typLevelTwo) {
        this.typLevelTwo = typLevelTwo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getModelNo() {
        return modelNo;
    }

    public void setModelNo(String modelNo) {
        this.modelNo = modelNo;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getBeiZhu() {
        return beiZhu;
    }

    public void setBeiZhu(String beiZhu) {
        this.beiZhu = beiZhu;
    }

//    public String getLoanType() {
//        return loanType;
//    }
//
//    public void setLoanType(String loanType) {
//        this.loanType = loanType;
//    }
}
