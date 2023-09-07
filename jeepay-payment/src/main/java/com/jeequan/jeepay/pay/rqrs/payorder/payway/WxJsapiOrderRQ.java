package com.jeequan.jeepay.pay.rqrs.payorder.payway;

import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/*
 * 支付方式： WX_JSAPI
 *
 * @author zhuxiao
 * @site https://www.jeequan.com
 * @date 2021/6/8 17:34
 */
@Data
public class WxJsapiOrderRQ extends UnifiedOrderRQ {

    /**
     * 微信openid
     **/
    @NotBlank(message = "openid不能为空")
    private String openid;

    /**
     * 标志是否为 subMchAppId的对应 openId， 0-否， 1-是， 默认否
     **/
    private Byte isSubOpenId;

    /**
     * 构造函数
     **/
    public WxJsapiOrderRQ() {
        this.setWayCode(CS.PAY_WAY_CODE.WX_JSAPI);
    }

    @Override
    public String getChannelUserId() {
        return this.openid;
    }
}
