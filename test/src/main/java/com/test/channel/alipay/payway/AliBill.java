package com.test.channel.alipay.payway;

import com.jeequan.jeepay.core.entity.PayOrder;
import com.test.channel.alipay.AlipayPaymentService;
import com.test.model.MchAppConfigContext;
import com.test.rqrs.AbstractRS;
import com.test.rqrs.payorder.UnifiedOrderRQ;
import com.test.rqrs.payorder.payway.AliBillOrderRS;
import org.springframework.stereotype.Service;

@Service("alipayPaymentByAliBillService") //Service Name需保持全局唯一性
public class AliBill extends AlipayPaymentService {
    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {
        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) {
        // 构造函数响应数据
        AliBillOrderRS res = new AliBillOrderRS();
        String url = sysConfigService.getDBApplicationConfig().getPaySiteUrl() + "/api/pay/bill/" + payOrder.getPayOrderId();
//        String url = "https://1y7037s862.goho.co/api/pay/bill/" + payOrder.getPayOrderId();
        // ↓↓↓↓↓↓ 调起接口成功后业务判断务必谨慎！！ 避免因代码编写bug，导致不能正确返回订单状态信息  ↓↓↓↓↓↓
        res.setCodeUrl(url);
        return res;
    }
}
