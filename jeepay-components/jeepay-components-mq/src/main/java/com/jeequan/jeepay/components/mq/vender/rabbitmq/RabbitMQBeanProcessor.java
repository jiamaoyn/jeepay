package com.jeequan.jeepay.components.mq.vender.rabbitmq;

import com.jeequan.jeepay.components.mq.constant.MQVenderCS;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 将spring容器的 [bean注册器]放置到属性中，为 RabbitConfig提供访问。
 * 顺序：
 * 1. postProcessBeanDefinitionRegistry (存放注册器)
 * 2. postProcessBeanFactory （没有使用）
 * 3. 注册延迟消息交换机的bean: delayedExchange
 * 4. 动态配置RabbitMQ所需的bean。
 */
@Configuration
@ConditionalOnProperty(name = MQVenderCS.YML_VENDER_KEY, havingValue = MQVenderCS.RABBIT_MQ)
public class RabbitMQBeanProcessor implements BeanDefinitionRegistryPostProcessor {

    /**
     * bean注册器
     **/
    protected BeanDefinitionRegistry beanDefinitionRegistry;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        this.beanDefinitionRegistry = beanDefinitionRegistry;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
    }

    /**
     * 自定义交换机： 用于延迟消息
     **/
    @Bean(name = RabbitMQConfig.DELAYED_EXCHANGE_NAME)
    CustomExchange delayedExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange(RabbitMQConfig.DELAYED_EXCHANGE_NAME, "x-delayed-message", true, false, args);
    }

}
