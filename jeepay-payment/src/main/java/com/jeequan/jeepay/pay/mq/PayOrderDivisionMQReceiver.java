package com.jeequan.jeepay.pay.mq;

import com.jeequan.jeepay.components.mq.model.PayOrderDivisionMQ;
import com.jeequan.jeepay.pay.service.PayOrderDivisionProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 接收MQ消息
 * 业务： 支付订单分账处理逻辑
 *
 * @author terrfly
 * @site https://www.jeequan.com
 * @date 2021/8/22 8:23
 */
@Slf4j
@Component
public class PayOrderDivisionMQReceiver implements PayOrderDivisionMQ.IMQReceiver {

    @Autowired
    private PayOrderDivisionProcessService payOrderDivisionProcessService;

    @Override
    public void receive(PayOrderDivisionMQ.MsgPayload payload) {

        try {
            log.info("接收订单分账通知MQ, msg={}", payload.toString());
            payOrderDivisionProcessService.processPayOrderDivision(payload.getPayOrderId(), payload.getUseSysAutoDivisionReceivers(), payload.getReceiverList(), payload.getIsResend());

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
