package com.jeequan.jeepay.components.mq.vender.activemq.receive;

import com.jeequan.jeepay.components.mq.constant.MQVenderCS;
import com.jeequan.jeepay.components.mq.model.ResetIsvMchAppInfoConfigMQ;
import com.jeequan.jeepay.components.mq.vender.IMQMsgReceiver;
import com.jeequan.jeepay.components.mq.vender.activemq.ActiveMQConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * activeMQ消息接收器：仅在vender=activeMQ时 && 项目实现IMQReceiver接口时 进行实例化
 * 业务：  更新服务商/商户/商户应用配置信息；
 */
@Component
@ConditionalOnProperty(name = MQVenderCS.YML_VENDER_KEY, havingValue = MQVenderCS.ACTIVE_MQ)
@ConditionalOnBean(ResetIsvMchAppInfoConfigMQ.IMQReceiver.class)
public class ResetIsvMchAppInfoActiveMQReceiver implements IMQMsgReceiver {

    @Autowired
    private ResetIsvMchAppInfoConfigMQ.IMQReceiver mqReceiver;

    /**
     * 接收 【 MQSendTypeEnum.BROADCAST  】 广播类型的消息
     **/
    @Override
    @JmsListener(destination = ResetIsvMchAppInfoConfigMQ.MQ_NAME, containerFactory = ActiveMQConfig.TOPIC_LISTENER_CONTAINER)
    public void receiveMsg(String msg) {
        mqReceiver.receive(ResetIsvMchAppInfoConfigMQ.parse(msg));
    }

}
