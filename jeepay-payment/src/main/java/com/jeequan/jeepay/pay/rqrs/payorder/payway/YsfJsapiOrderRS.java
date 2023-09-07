package com.jeequan.jeepay.pay.rqrs.payorder.payway;

import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.utils.JsonKit;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRS;
import lombok.Data;

/*
 * 支付方式： YSF_JSAPI
 * @date 2021/6/8 17:34
 */
@Data
public class YsfJsapiOrderRS extends UnifiedOrderRS {

    /**
     * 调起支付插件的云闪付订单号
     **/
    private String redirectUrl;

    @Override
    public String buildPayDataType() {
        return CS.PAY_DATA_TYPE.YSF_APP;
    }

    @Override
    public String buildPayData() {
        return JsonKit.newJson("redirectUrl", redirectUrl).toString();
    }

}
