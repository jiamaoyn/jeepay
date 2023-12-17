package com.jeetask.telegram.mq;

import com.jeequan.jeepay.components.mq.model.ResetIsvMchAppInfoConfigMQ;
import com.jeetask.telegram.service.TelegramService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 接收MQ消息
 * 业务： 更新应用配置信息；
 */
@Slf4j
@Component
public class ResetIsvMchAppInfoMQReceiver implements ResetIsvMchAppInfoConfigMQ.IMQReceiver {

    @Autowired
    private TelegramService telegramService;

    @Override
    public void receive(ResetIsvMchAppInfoConfigMQ.MsgPayload payload) {

        if (payload.getResetType() == ResetIsvMchAppInfoConfigMQ.RESET_TYPE_TELEGRAM_APP) {
            this.sendMessageSys(payload.getMsg(), payload.getMchNo());
        }

    }

    /**
     * 接收 [商户应用支付参数配置信息] 的消息
     **/
    private void sendMessageSys(String msg, String mchNo) {
        telegramService.sendMessageSys(msg);
        telegramService.sendMessage(mchNo, msg);
    }

}
