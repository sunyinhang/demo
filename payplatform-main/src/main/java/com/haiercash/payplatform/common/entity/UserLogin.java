/*
 * 功  能：简单说明该类的功能
 * 
 * 文件名：UserLogin.java
 * 
 * 描  述：
 * 
 * [变更履历]
 * Version   变更日         部署              作者           变更内容
 * -----------------------------------------------------------------------------
 * V1.00     2015年12月24日   Haiercash    xuchao      CREATE
 * -----------------------------------------------------------------------------
 *
 *
 * Copyright (c) 2015 Haiercash All Rights Reserved.
 *┌─────────────────────────────────────────────────—────┐
 *│ 版权声明                               Haiercash       │
 *└──────────────────────────────—————————————————————───┘
 */

package com.haiercash.payplatform.common.entity;


import lombok.Data;
import lombok.ToString;

/**
 * DESCRIPTION:客户登录
 * <p>
 * <p>
 * <a href="UserLogin.java"><i>View Source</i></a>
 * </p>
 *
 * @author <a href="mailto:xuchao@haiercash.com">xuchao</a>
 * @version Revision: 1.0  Date: 2015年12月24日 上午9:44:34
 */
@Data
@ToString
public class UserLogin {
    private String userName;
    private String passWord;
    private String flag;
    private String signTime;
}
