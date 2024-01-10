package com.jeequan.jeepay.pay.channel.alipay.payway;

import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.params.alipay.AlipayIsvsubMchParams;
import com.jeequan.jeepay.core.model.params.alipay.AlipayNormalMchParams;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.pay.channel.alipay.AlipayPaymentService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.AbstractRS;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.AliBillOrderRS;
import com.jeequan.jeepay.service.impl.PayOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("alipayPaymentByAliBillService") //Service Name需保持全局唯一性
public class AliBill extends AlipayPaymentService {

    @Autowired
    private PayOrderService payOrderService;
    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {
        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) {
        // 构造函数响应数据
        AliBillOrderRS res = new AliBillOrderRS();
        if (payOrder == null) {
            throw new BizException("订单不存在");
        }
        String pid;
        if (!mchAppConfigContext.isIsvsubMch()){
            AlipayNormalMchParams normalMchParams = (AlipayNormalMchParams) configContextQueryService.queryNormalMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), "alipay");
            if (normalMchParams == null) {
                throw new BizException("商户支付宝接口没有配置！");
            }
            pid = normalMchParams.getPid();
        } else {
            AlipayIsvsubMchParams normalMchParams = (AlipayIsvsubMchParams) configContextQueryService.queryIsvsubMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), "alipay");
            if (normalMchParams == null) {
                throw new BizException("商户支付宝接口没有配置！");
            }
            pid = normalMchParams.getUserId();
        }
        String url = "https://www.alipay.com?appId=20000116&actionType=toAccount&sourceId=contactStage&chatUserId="+pid+"&displayName=TK&chatUserName=TK&chatUserType=1&skipAuth=true&amount="+ AmountUtil.convertCent2Dollar(payOrder.getAmount().toString())+"&memo="+payOrder.getPayOrderId();
        // ↓↓↓↓↓↓ 调起接口成功后业务判断务必谨慎！！ 避免因代码编写bug，导致不能正确返回订单状态信息  ↓↓↓↓↓↓
        res.setCodeUrl(url);
        if (payOrder.getState() == PayOrder.STATE_INIT){
            boolean isSuccess = payOrderService.updateInit2Ing(payOrder.getPayOrderId(), payOrder);
            if (!isSuccess) {
                throw new BizException("更新订单异常!");
            }
        }
        return res;
    }
}
