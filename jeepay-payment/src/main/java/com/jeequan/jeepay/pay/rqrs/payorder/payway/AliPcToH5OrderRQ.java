package com.jeequan.jeepay.pay.rqrs.payorder.payway;

import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.pay.rqrs.payorder.CommonPayDataRQ;
import lombok.Data;

@Data
public class AliPcToH5OrderRQ  extends CommonPayDataRQ {

    /**
     * 构造函数
     **/
    public AliPcToH5OrderRQ() {
        this.setWayCode(CS.PAY_WAY_CODE.ALI_PC_TO_H5);
    }
}
