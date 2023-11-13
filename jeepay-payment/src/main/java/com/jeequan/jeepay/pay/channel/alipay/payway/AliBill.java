package com.jeequan.jeepay.pay.channel.alipay.payway;

import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.pay.channel.alipay.AlipayPaymentService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.AbstractRS;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.AliBillOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.AliBillOrderRS;
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
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        String url = sysConfigService.getDBApplicationConfig().getPaySiteUrl() + "/api/pay/bill/" + payOrder.getPayOrderId();
        // ↓↓↓↓↓↓ 调起接口成功后业务判断务必谨慎！！ 避免因代码编写bug，导致不能正确返回订单状态信息  ↓↓↓↓↓↓
        res.setCodeUrl(url);
        channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
        res.setChannelRetMsg(channelRetMsg);
        return res;
    }
}
