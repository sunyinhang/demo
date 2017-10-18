package com.haiercash.payplatform.common.data;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by 许崇雷 on 2017-10-10.
 */
@Data
@Entity
@Table(name = "ENTRY_SETTING")
public class EntrySetting {
    @Id
    private String channelNo;
    private String activityUrl;//活动页地址   （域名省略，例如：xjd/#!/applyQuota/applyQuota.html）
    private String loginType;//01 登陆注册 02 三方免登陆
    private String verifyUrlService;//第三方验证的 service 的bean 目前支持: haierThirdTokenVerifyService
    private String verifyUrlThird;//第三方token 验证地址 举例:  http://xxx/userinfo?access_token=
    private String loginUrl;//登陆地址 举例: http://xx/login.html?token=
    private String loanTypeFrom;//贷款种类来源 01:channelNo 从后台取  02:根据 custName 从 CRM 取
    private String enabled;//Y 启用 N禁用
}
