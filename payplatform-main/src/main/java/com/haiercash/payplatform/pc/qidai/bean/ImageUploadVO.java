/*
 * 功  能：简单说明该类的功能
 *
 * 文件名：OrangeImageVO.java
 *
 * 描  述：
 *
 * [变更履历]
 * Version   变更日         部署              作者           变更内容
 * -----------------------------------------------------------------------------
 * V1.00     2015年8月18日   haiercash    xuchao      CREATE
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

/**
 * DESCRIPTION:
 * <p>
 * <p>
 * <a href="OrangeImageVO.java"><i>View Source</i></a>
 * </p>
 *
 * @author <a href="mailto:xuchao@haiercash.com">xuchao</a>
 * @version Revision: 1.0  Date: 2015年8月18日 下午9:23:36
 */
@Data
public class ImageUploadVO {
    private String filename;
    private String fileoper;
    private String fileType;
    private String file;
    private String md5;
}
