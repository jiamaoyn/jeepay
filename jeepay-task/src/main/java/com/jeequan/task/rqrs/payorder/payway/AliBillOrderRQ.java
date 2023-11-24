package com.jeequan.task.rqrs.payorder.payway;

import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.task.rqrs.payorder.UnifiedOrderRQ;
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