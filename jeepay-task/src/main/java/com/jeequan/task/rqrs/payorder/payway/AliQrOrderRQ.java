package com.jeequan.task.rqrs.payorder.payway;

import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.task.rqrs.payorder.CommonPayDataRQ;
import lombok.Data;

/*
 * 支付方式： ALI_QR
 * @date 2021/6/8 17:34
 */
@Data
public class AliQrOrderRQ extends CommonPayDataRQ {

    /**
     * 构造函数
     **/
    public AliQrOrderRQ() {
        this.setWayCode(CS.PAY_WAY_CODE.ALI_QR);
    }

}
