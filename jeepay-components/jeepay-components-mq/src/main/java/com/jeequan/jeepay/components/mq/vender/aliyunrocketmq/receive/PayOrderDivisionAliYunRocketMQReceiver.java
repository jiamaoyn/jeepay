package com.jeequan.jeepay.components.mq.vender.aliyunrocketmq.receive;

import com.jeequan.jeepay.components.mq.constant.MQVenderCS;
import com.jeequan.jeepay.components.mq.model.PayOrderDivisionMQ;
import com.jeequan.jeepay.components.mq.vender.aliyunrocketmq.AbstractAliYunRocketMQReceiver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * AliYunRocketMQ消息接收器：仅在vender=AliYunRocketMQ时 && 项目实现IMQReceiver接口时 进行实例化
 * 业务：  支付订单分账通知
 */
@Slf4j
@Component
@ConditionalOnProperty(name = MQVenderCS.YML_VENDER_KEY, havingValue = MQVenderCS.ALIYUN_ROCKET_MQ)
@ConditionalOnBean(PayOrderDivisionMQ.IMQReceiver.class)
public class PayOrderDivisionAliYunRocketMQReceiver extends AbstractAliYunRocketMQReceiver {

    private static final String CONSUMER_NAME = "支付订单分账消息";

    @Autowired
    private PayOrderDivisionMQ.IMQReceiver mqReceiver;

    /**
     * 接收 【 queue 】 类型的消息
     **/
    @Override
    public void receiveMsg(String msg) {
        mqReceiver.receive(PayOrderDivisionMQ.parse(msg));
    }

    /**
     * 获取topic名称
     */
    @Override
    public String getMQName() {
        return PayOrderDivisionMQ.MQ_NAME;
    }

    /**
     * 获取业务名称
     */
    @Override
    public String getConsumerName() {
        return CONSUMER_NAME;
    }

}
