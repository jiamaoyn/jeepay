package com.test.rqrs.payorder.payway;

import com.jeequan.jeepay.core.constants.CS;
import com.test.rqrs.payorder.UnifiedOrderRS;
import lombok.Data;

/*
 * 支付方式： WX_LITE
 *
 * @author zhuxiao
 * @site https://www.jeequan.com
 * @date 2021/6/8 17:34
 */
@Data
public class WxLiteOrderRS extends UnifiedOrderRS {

    /**
     * 预支付数据包
     **/
    private String payInfo;

    @Override
    public String buildPayDataType() {
        return CS.PAY_DATA_TYPE.WX_APP;
    }

    @Override
    public String buildPayData() {
        return payInfo;
    }

}
