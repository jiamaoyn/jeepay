package com.jeequan.jeepay.pay.rqrs.payorder.payway;

import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.utils.JsonKit;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRS;
import lombok.Data;

/*
 * 支付方式： ALI_LITE
 * @date 2021/6/8 17:34
 */
@Data
public class AliLiteOrderRS extends UnifiedOrderRS {

    /**
     * 调起支付插件的支付宝订单号
     **/
    private String alipayTradeNo;

    @Override
    public String buildPayDataType() {
        return CS.PAY_DATA_TYPE.ALI_APP;
    }

    @Override
    public String buildPayData() {
        return JsonKit.newJson("alipayTradeNo", alipayTradeNo).toString();
    }

}
