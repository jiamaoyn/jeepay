package com.test.channel.alipay;

import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.test.channel.AbstractPaymentService;
import com.test.model.MchAppConfigContext;
import com.test.rqrs.AbstractRS;
import com.test.rqrs.payorder.UnifiedOrderRQ;
import com.test.util.PaywayUtil;
import org.springframework.stereotype.Service;

/*
 * 支付接口： 支付宝官方
 * 支付方式： 自适应
 * @date 2021/6/8 17:19
 */
@Service
public class AlipayPaymentService extends AbstractPaymentService {

    @Override
    public String getIfCode() {
        return CS.IF_CODE.ALIPAY;
    }

    @Override
    public boolean isSupport(String wayCode) {
        return true;
    }

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {
        return PaywayUtil.getRealPaywayService(this, payOrder.getWayCode()).preCheck(rq, payOrder);
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception {
        return PaywayUtil.getRealPaywayService(this, payOrder.getWayCode()).pay(rq, payOrder, mchAppConfigContext);
    }

}
