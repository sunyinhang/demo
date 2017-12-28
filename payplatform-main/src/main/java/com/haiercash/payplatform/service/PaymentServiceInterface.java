/*
 * 功  能：简单说明该类的功能
 * 
 * 文件名：PaymentServiceInterface.java
 * 
 * 描  述：
 * 
 * [变更履历]
 * Version   变更日         部署              作者           变更内容
 * -----------------------------------------------------------------------------
 * V1.00     2015年12月23日   Haiercash    suyang      CREATE
 * -----------------------------------------------------------------------------
 *
 *
 * Copyright (c) 2015 Haiercash All Rights Reserved.
 *┌─────────────────────────────────────────────────—────┐
 *│ 版权声明                               Haiercash       │
 *└──────────────────────────────—————————————————————───┘
 */

package com.haiercash.payplatform.service;


import com.haiercash.payplatform.common.entity.QueryLimitMessage;
import com.haiercash.payplatform.common.entity.ReturnMessage;

/**
 *  DESCRIPTION:
 *
 * <p>
 * <a href="PaymentServiceInterface.java"><i>View Source</i></a>
 * </p>
 * 
 * @author <a href="mailto:suyang@haiercash.com">suyang</a>
 *
 * @version Revision: 1.0  Date: 2015年12月23日 下午2:02:16 
 *
 */
public interface PaymentServiceInterface {
	ReturnMessage queryLimitMessage(QueryLimitMessage queryLimitMessage);//额度申请信息查询
}
