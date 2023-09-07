package com.jeequan.jeepay.pay.rqrs.payorder.payway;

import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.utils.JsonKit;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRS;
import lombok.Data;

/*
 * 支付方式： UP_JSAPI
 *
 * @author jmdhappy
 * @site https://www.jeequan.com
 * @date 2022/3/17 12:34
 */
@Data
public class UpJsapiOrderRS extends UnifiedOrderRS {

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
