package com.haiercash.common.data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by yinjun on 2016/8/10.
 */
@Entity
@Table(name="KEY_WORDS")
public class KeyWords {
    @Id
    private String seqId;//id主键
    private String keyWord;//关键词
    private String isTrue;//是否启用

    public String getIsTrue() {
        return isTrue;
    }

    public void setIsTrue(String isTrue) {
        this.isTrue = isTrue;
    }

    public String getKeyWord() {
        return keyWord;
    }

    public void setKeyWord(String keyWord) {
        this.keyWord = keyWord;
    }

    public String getSeqId() {
        return seqId;
    }

    public void setSeqId(String seqId) {
        this.seqId = seqId;
    }

    @Override
    public String toString() {
        return "KeyWords{" +
                "isTrue='" + isTrue + '\'' +
                ", seqId='" + seqId + '\'' +
                ", keyWord='" + keyWord + '\'' +
                '}';
    }
}
