package com.haiercash.appserver.util.sign;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "uauth_ca_sign_agreement")
public class FileSignAgreement {
    @Id
    // @GenericGenerator(name="idGenerator", strategy="uuid")
    // @GeneratedValue(generator="idGenerator")
    private String id;
    private String tempSeriNum; // 模板编号.
    private String templateFileName; // 模板文件名.
    private String userPage; // 客户签名页码.
    private String userX; // 客户签名X坐标.
    private String userY; // 客户签名Y坐标.
    private String coPage; // 单位签章页码.
    private String coX; // 单位签章X坐标.
    private String coY; // 单位签章Y坐标.
    private String useCoSign; // 是否进行单位签章.

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTemplateFileName() {
        return templateFileName;
    }

    public void setTemplateFileName(String templateFileName) {
        this.templateFileName = templateFileName;
    }

    public String getUserPage() {
        return userPage;
    }

    public void setUserPage(String userPage) {
        this.userPage = userPage;
    }

    public String getUserX() {
        return userX;
    }

    public void setUserX(String userX) {
        this.userX = userX;
    }

    public String getUserY() {
        return userY;
    }

    public void setUserY(String userY) {
        this.userY = userY;
    }

    public String getCoPage() {
        return coPage;
    }

    public void setCoPage(String coPage) {
        this.coPage = coPage;
    }

    public String getCoX() {
        return coX;
    }

    public void setCoX(String coX) {
        this.coX = coX;
    }

    public String getCoY() {
        return coY;
    }

    public void setCoY(String coY) {
        this.coY = coY;
    }

    public String getTempSeriNum() {
        return tempSeriNum;
    }

    public void setTempSeriNum(String tempSeriNum) {
        this.tempSeriNum = tempSeriNum;
    }

    public String getUseCoSign() {
        return useCoSign;
    }

    public void setUseCoSign(String useCoSign) {
        this.useCoSign = useCoSign;
    }
}
