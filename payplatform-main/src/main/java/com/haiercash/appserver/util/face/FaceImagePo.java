/*
 * 功  能：简单说明该类的功能
 * 
 * 文件名：FaceImagePo.java
 * 
 * 描  述：
 * 
 * [变更履历]
 * Version   变更日         部署              作者           变更内容
 * -----------------------------------------------------------------------------
 * V1.00     2016年6月10日   Haiercash    xuchao      CREATE
 * -----------------------------------------------------------------------------
 *
 *
 * Copyright (c) 2016 Haiercash All Rights Reserved.
 *┌─────────────────────────────────────────────────—────┐
 *│ 版权声明                               Haiercash       │
 *└──────────────────────────────—————————————————————───┘
 */

package com.haiercash.appserver.util.face;

/**
 * DESCRIPTION:
 * <p>
 * <p>
 * <a href="FaceImagePo.java"><i>View Source</i></a>
 * </p>
 *
 * @author <a href="mailto:xuchao@haiercash.com">xuchao</a>
 * @version Revision: 1.0 Date: 2016年6月10日 下午4:49:36
 */

public class FaceImagePo {
    private String personalName;// 客户姓名
    private String identityCardNo;// 客户身份证
    private String filestream;// 文件base64
    private String appno;// 申请编号
    private String apptime;// 申请时间
    private String filestreamname;// 文件名
    private String organization; //机构代码

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getFilestreamname() {
        return filestreamname;
    }

    public void setFilestreamname(String filestreamname) {
        this.filestreamname = filestreamname;
    }

    public String getPersonalName() {
        return personalName;
    }

    public void setPersonalName(String personalName) {
        this.personalName = personalName;
    }

    public String getIdentityCardNo() {
        return identityCardNo;
    }

    public void setIdentityCardNo(String identityCardNo) {
        this.identityCardNo = identityCardNo;
    }

    public String getFilestream() {
        return filestream;
    }

    public void setFilestream(String filestream) {
        this.filestream = filestream;
    }

    public String getAppno() {
        return appno;
    }

    public void setAppno(String appno) {
        this.appno = appno;
    }

    public String getApptime() {
        return apptime;
    }

    public void setApptime(String apptime) {
        this.apptime = apptime;
    }

    @Override
    public String toString() {
        return "FaceImagePo{" +
                "personalName='" + personalName + '\'' +
                ", identityCardNo='" + identityCardNo + '\'' +
                ", appno='" + appno + '\'' +
                ", apptime='" + apptime + '\'' +
                ", filestreamname='" + filestreamname + '\'' +
                ", organization='" + organization + '\'' +
                '}';
    }
}
