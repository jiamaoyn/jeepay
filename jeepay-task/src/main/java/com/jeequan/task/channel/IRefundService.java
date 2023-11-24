package com.jeequan.task.channel;

import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.RefundOrder;
import com.jeequan.task.model.MchAppConfigContext;
import com.jeequan.task.rqrs.msg.ChannelRetMsg;
import com.jeequan.task.rqrs.refund.RefundOrderRQ;

/*
 * 调起上游渠道侧退款接口
 * @date 2021/6/17 9:35
 */
public interface IRefundService {

    /**
     * 获取到接口code
     **/
    String getIfCode();

    /**
     * 前置检查如参数等信息是否符合要求， 返回错误信息或直接抛出异常即可
     */
    String preCheck(RefundOrderRQ bizRQ, RefundOrder refundOrder, PayOrder payOrder);

    /**
     * 调起退款接口，并响应数据；  内部处理普通商户和服务商模式
     **/
    ChannelRetMsg refund(RefundOrderRQ bizRQ, RefundOrder refundOrder, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception;

    /**
     * 退款查单接口
     **/
    ChannelRetMsg query(RefundOrder refundOrder, MchAppConfigContext mchAppConfigContext) throws Exception;

}
