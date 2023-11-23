package com.jeequan.jeepay.pay.channel.wxpay.paywayV3;


import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.pay.channel.wxpay.WxpayPaymentService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.AbstractRS;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/*
 * 微信 条码支付
 *
 * @author zhuxiao
 * @site https://www.jeequan.com
 * @date 2021/6/8 18:08
 */
@Service("wxpayPaymentByBarV3Service") //Service Name需保持全局唯一性
public class WxBar extends WxpayPaymentService {

    @Autowired
    private com.jeequan.jeepay.pay.channel.wxpay.payway.WxBar wxBar;

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {
        return wxBar.preCheck(rq, payOrder);
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception {
        return wxBar.pay(rq, payOrder, mchAppConfigContext);
    }
}
