package com.jeequan.jeepay.pay.rqrs.payorder.payway;

import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.pay.rqrs.payorder.CommonPayDataRQ;
import lombok.Data;

/*
 * 支付方式： UPACP_WAP
 *
 * @author jmdhappy
 * @site https://www.jeequan.com
 * @date 2021/12/1 19:57
 */
@Data
public class UpWapOrderRQ extends CommonPayDataRQ {

    /**
     * 构造函数
     **/
    public UpWapOrderRQ() {
        this.setWayCode(CS.PAY_WAY_CODE.UP_WAP);
    }

}
