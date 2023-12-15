package com.jeequan.jeepay.pay.ctrl.telegram;

import com.alipay.api.domain.AccountLogItemResult;
import com.jeequan.jeepay.core.ctrls.AbstractCtrl;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.pay.service.ChannelOrderReissueService;
import com.jeequan.jeepay.service.impl.PayOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/telegram")
public class TelegramQueryOrderController extends AbstractCtrl {
    @Autowired
    private PayOrderService payOrderService;
    @Autowired
    private ChannelOrderReissueService channelOrderReissueService;
    @RequestMapping(value = "/orderNch/{mchOrderNo}/{channelOrderNo}")
    private ApiRes<Object> toPayForm(@PathVariable("mchOrderNo") String mchOrderNo, @PathVariable("channelOrderNo") String channelOrderNo) {
        PayOrder payOrder = payOrderService.queryMchOrderNo(mchOrderNo);
        if (payOrder.getWayCode().equals("ALI_BILL")){
            payOrder.setChannelOrderNo(channelOrderNo);
            AccountLogItemResult accountLogItemResult = channelOrderReissueService.processPayOrderBillTelegramBot(payOrder);
            return ApiRes.ok(accountLogItemResult);
        }
        return ApiRes.ok("订单号不匹配或已支付成功");
    }
}
