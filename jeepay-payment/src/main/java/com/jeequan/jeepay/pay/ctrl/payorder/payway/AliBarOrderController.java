package com.jeequan.jeepay.pay.ctrl.payorder.payway;

import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.pay.ctrl.payorder.AbstractPayOrderController;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.AliBarOrderRQ;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * 支付宝 条码支付 controller
 * @date 2021/6/8 17:25
 */
@Slf4j
@RestController
public class AliBarOrderController extends AbstractPayOrderController {


    /**
     * 统一下单接口
     **/
    @PostMapping("/api/pay/aliBarOrder")
    public ApiRes aliBarOrder() {

        //获取参数 & 验证
        AliBarOrderRQ bizRQ = getRQByWithMchSign(AliBarOrderRQ.class);

        // 统一下单接口
        return unifiedOrder(CS.PAY_WAY_CODE.ALI_BAR, bizRQ);

    }


}
