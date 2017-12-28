package com.haiercash.payplatform.common.data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "bc_bank_info")
public class BcBankInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String cardSign;
    private Integer cardSignLen;
    private Integer cardNoLen;
    private String cardType;
    private String cardName;
    private String bankNo;
    private String bankName;

    public String getCardSign() {
        return cardSign;
    }

    public void setCardSign(String cardSign) {
        this.cardSign = cardSign;
    }

    public Integer getCardSignLen() {
        return cardSignLen;
    }

    public void setCardSignLen(Integer cardSignLen) {
        this.cardSignLen = cardSignLen;
    }

    public Integer getCardNoLen() {
        return cardNoLen;
    }

    public void setCardNoLen(Integer cardNoLen) {
        this.cardNoLen = cardNoLen;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public String getBankNo() {
        return bankNo;
    }

    public void setBankNo(String bankNo) {
        this.bankNo = bankNo;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
}