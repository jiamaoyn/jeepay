package com.jeequan.jeepay.components.mq.vender.aliyunrocketmq.receive;

import com.jeequan.jeepay.components.mq.constant.MQSendTypeEnum;
import com.jeequan.jeepay.components.mq.constant.MQVenderCS;
import com.jeequan.jeepay.components.mq.model.ResetIsvMchAppInfoConfigMQ;
import com.jeequan.jeepay.components.mq.vender.aliyunrocketmq.AbstractAliYunRocketMQReceiver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * AliYunRocketMQ消息接收器：仅在vender=AliYunRocketMQ时 && 项目实现IMQReceiver接口时 进行实例化
 * 业务：  更新服务商/商户/商户应用配置信息
 */
@Slf4j
@Component
@ConditionalOnProperty(name = MQVenderCS.YML_VENDER_KEY, havingValue = MQVenderCS.ALIYUN_ROCKET_MQ)
@ConditionalOnBean(ResetIsvMchAppInfoConfigMQ.IMQReceiver.class)
public class ResetIsvMchAppInfoAliYunRocketMQReceiver extends AbstractAliYunRocketMQReceiver {

    private static final String CONSUMER_NAME = "更新服务商/商户/商户应用配置信息消息";

    @Autowired
    private ResetIsvMchAppInfoConfigMQ.IMQReceiver mqReceiver;

    /**
     * 接收 【 MQSendTypeEnum.BROADCAST  】 广播类型的消息
     * <p>
     * 注意：
     * AliYunRocketMQ的广播模式（fanout）交换机 --》全部的Queue
     * 如果queue包含多个消费者， 【例如，manager和payment的监听器是名称相同的queue下的消费者（Consumers） 】， 两个消费者是工作模式且存在竞争关系， 导致只能一个来消费。
     * 解决：
     * 每个topic的QUEUE都声明一个FANOUT交换机， 消费者声明一个系统产生的【随机队列】绑定到这个交换机上，然后往交换机发消息，只要绑定到这个交换机上都能收到消息。
     * 参考： https://bbs.csdn.net/topics/392509262?list=70088931
     **/
    @Override
    public void receiveMsg(String msg) {
        mqReceiver.receive(ResetIsvMchAppInfoConfigMQ.parse(msg));
    }

    /**
     * 获取topic名称
     */
    @Override
    public String getMQName() {
        return ResetIsvMchAppInfoConfigMQ.MQ_NAME;
    }

    /**
     * 获取业务名称
     */
    @Override
    public String getConsumerName() {
        return CONSUMER_NAME;
    }

    /**
     * 发送类型
     */
    @Override
    public MQSendTypeEnum getMQType() {
        // RocketMQ的广播模式
        return MQSendTypeEnum.BROADCAST;
    }

}
