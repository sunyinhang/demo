/*
 * 功  能：简单说明该类的功能
 *
 * 文件名：ChinaTelecomLoanApplicationBean.java
 *
 * 描  述：
 *
 * [变更履历]
 * Version   变更日         部署              作者           变更内容
 * -----------------------------------------------------------------------------
 * V1.00     2015年6月12日   haiercash    xuchao      CREATE
 * -----------------------------------------------------------------------------
 *
 *
 * Copyright (c) 2015 haiercash All Rights Reserved.
 *┌─────────────────────────────────────────────────—────┐
 *│ 版权声明                               haiercash       │
 *└──────────────────────────────—————————————————————───┘
 */

package com.haiercash.payplatform.pc.qidai.bean;

import lombok.Data;

import java.util.List;

/**
 * DESCRIPTION:附件上传pojo
 * <p>
 * <a href="OrangeLoanApplicationBean.java"><i>View Source</i></a>
 * </p>
 *
 * @author <a href="mailto:xuchao@haiercash.com">xuchao</a>
 * @version Revision: 1.0  Date: 2015年8月11日 上午11:09:33
 */
@Data
public class ImageUploadPO {
    private byte[] securityKey;//签名
    private String channelNo;//渠道编码
    private String appno;//姓名
    private String apptime;//性别
    private String appl;//身份证号
    private List<ImageUploadVO> filelist;//身份证号
}
