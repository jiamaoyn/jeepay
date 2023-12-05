package com.test.rqrs.payorder.payway;

import com.jeequan.jeepay.core.constants.CS;
import com.test.rqrs.payorder.UnifiedOrderRQ;
import lombok.Data;

@Data
public class AliBillOrderRQ extends UnifiedOrderRQ {

    /**
     * 构造函数
     **/
    public AliBillOrderRQ() {
        this.setWayCode(CS.PAY_WAY_CODE.ALI_BILL);
    }

}