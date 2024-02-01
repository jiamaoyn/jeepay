package com.jeequan.jeepay.pay.channel.alipay.payway;

import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.pay.channel.alipay.AlipayPaymentService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.AbstractRS;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.AliPcToH5OrderRS;
import org.springframework.stereotype.Service;
/*
 * 支付宝 PC转H5支付
 * @date 2021/6/8 17:21
 */
@Service("alipayPaymentByAliPcToH5Service") //Service Name需保持全局唯一性
public class AliPcToH5 extends AlipayPaymentService {

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {
        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) {
        AliPcToH5OrderRS res = new AliPcToH5OrderRS();
        if (payOrder == null) {
            throw new BizException("订单不存在");
        }
        String url = "alipayqr://platformapi/startapp?saId=10000007&qrcode=https%3A%2F%2Fpayhtml.ilian8html.icu%2Fapi%2Fpay%2Fali_pc_app_pay%2F"+payOrder.getPayOrderId();
        // ↓↓↓↓↓↓ 调起接口成功后业务判断务必谨慎！！ 避免因代码编写bug，导致不能正确返回订单状态信息  ↓↓↓↓↓↓
        res.setPayUrl(url);

        return res;
    }

}
