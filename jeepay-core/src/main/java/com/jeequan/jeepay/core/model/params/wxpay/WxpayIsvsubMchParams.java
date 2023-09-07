package com.jeequan.jeepay.core.model.params.wxpay;

import com.jeequan.jeepay.core.model.params.IsvsubMchParams;
import lombok.Data;

/*
 * 微信官方支付 配置参数
 *
 * @author zhuxiao
 * @site https://www.jeequan.com
 * @date 2021/6/8 18:02
 */
@Data
public class WxpayIsvsubMchParams extends IsvsubMchParams {

    /**
     * 子商户ID
     **/
    private String subMchId;

    /**
     * 子账户appID
     **/
    private String subMchAppId;


}
