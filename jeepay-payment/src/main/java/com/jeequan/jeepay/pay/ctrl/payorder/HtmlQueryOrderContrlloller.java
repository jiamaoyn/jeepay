package com.jeequan.jeepay.pay.ctrl.payorder;

import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.params.alipay.AlipayIsvsubMchParams;
import com.jeequan.jeepay.core.model.params.alipay.AlipayNormalMchParams;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.pay.ctrl.ApiController;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.service.ConfigContextQueryService;
import com.jeequan.jeepay.service.impl.PayOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.net.URLEncoder;

@Slf4j
@Controller
public class HtmlQueryOrderContrlloller extends ApiController {
    @Autowired
    private PayOrderService payOrderService;
    @Autowired
    private ConfigContextQueryService configContextQueryService;

    @RequestMapping("/api/pay/bill_pay/{payOrderId}")
    public String getAliPayBill(@PathVariable("payOrderId") String payOrderId) throws IOException {
        PayOrder payOrder = payOrderService.queryMchOrder(payOrderId);
        if (payOrder == null) {
            throw new BizException("订单不存在");
        }
        //获取支付参数 (缓存数据) 和 商户信息
        MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(payOrder.getMchNo(), payOrder.getAppId());
        if (mchAppConfigContext == null) {
            throw new BizException("获取商户应用信息失败");
        }
        String pid;
        String aliName = null;
        if (!mchAppConfigContext.isIsvsubMch()){
            AlipayNormalMchParams normalMchParams = (AlipayNormalMchParams) configContextQueryService.queryNormalMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), "alipay");
            if (normalMchParams == null) {
                throw new BizException("商户支付宝接口没有配置！");
            }
            pid = normalMchParams.getPid();
            aliName = normalMchParams.getAliName();
        } else {
            AlipayIsvsubMchParams normalMchParams = (AlipayIsvsubMchParams) configContextQueryService.queryIsvsubMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), "alipay");
            if (normalMchParams == null) {
                throw new BizException("商户支付宝接口没有配置！");
            }
            pid = normalMchParams.getUserId();
        }
        if (payOrder.getState() == PayOrder.STATE_INIT){
            boolean isSuccess = payOrderService.updateInit2Ing(payOrder.getPayOrderId(), payOrder);
            if (!isSuccess) {
                throw new BizException("更新订单异常!");
            }
        }
        PayOrder payOrder1 = new PayOrder();
        payOrder1.setState(payOrder.getState());
        payOrder1.setPayOrderId(payOrderId);
        payOrder1.setIfCode(payOrder.getIfCode());
        request.setAttribute("pid", pid);
        if (aliName!=null)request.setAttribute("aliName", aliName);
        request.setAttribute("amount", AmountUtil.convertCent2Dollar(payOrder.getAmount()));
        payOrder1.setReturnUrl("alipayqr://platformapi/startapp?saId=10000007&qrcode="+ URLEncoder.encode("https://" + request.getServerName() + "/api/pay/bill/"+payOrderId, "UTF-8"));
        request.setAttribute("payOrder", payOrder1);
        return "pay/pay";
    }
}
