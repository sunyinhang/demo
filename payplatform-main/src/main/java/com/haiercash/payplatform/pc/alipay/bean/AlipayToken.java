package com.haiercash.payplatform.pc.alipay.bean;

import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.haiercash.core.lang.Convert;
import com.haiercash.core.time.DateUtils;
import lombok.Data;
import org.springframework.util.Assert;

import java.util.Date;

/**
 * Created by 许崇雷 on 2018-01-19.
 */
@Data
public class AlipayToken {
    private static final int EXPIRES_AHEAD = 30;//token 提前过期 ,分钟
    private String userId;
    private String token;
    private Integer expiresIn; //单位秒
    private String refreshToken;
    private Integer refreshTokenExpiresIn;//单位秒
    private Date genTime;

    public AlipayToken(AlipaySystemOauthTokenResponse response) {
        Assert.notNull(response, "response can not be null");
        this.userId = response.getUserId();
        this.token = response.getAccessToken();
        this.expiresIn = Convert.nullInteger(response.getExpiresIn());
        this.refreshToken = response.getRefreshToken();
        this.refreshTokenExpiresIn = Convert.nullInteger(response.getReExpiresIn());
        this.genTime = DateUtils.now();
    }

    public boolean isValid() {
        return this.genTime != null && this.expiresIn != null && System.currentTimeMillis() <= (this.genTime.getTime() + this.expiresIn * 1000 - EXPIRES_AHEAD * 60 * 1000);
    }

    public boolean isRefreshValid() {
        return this.genTime != null && this.refreshTokenExpiresIn != null && System.currentTimeMillis() <= (this.genTime.getTime() + this.refreshTokenExpiresIn * 1000);
    }
}
