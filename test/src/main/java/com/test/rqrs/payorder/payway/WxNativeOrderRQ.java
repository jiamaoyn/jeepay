package com.test.rqrs.payorder.payway;

import com.jeequan.jeepay.core.constants.CS;
import com.test.rqrs.payorder.CommonPayDataRQ;
import lombok.Data;

/*
 * 支付方式： WX_NATIVE
 *
 * @author zhuxiao
 * @site https://www.jeequan.com
 * @date 2021/6/8 17:34
 */
@Data
public class WxNativeOrderRQ extends CommonPayDataRQ {

    /**
     * 构造函数
     **/
    public WxNativeOrderRQ() {
        this.setWayCode(CS.PAY_WAY_CODE.WX_NATIVE);
    }

}
