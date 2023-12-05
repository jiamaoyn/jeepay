package com.test.channel;

import com.jeequan.jeepay.core.entity.PayOrder;
import com.test.model.MchAppConfigContext;
import com.test.rqrs.msg.ChannelRetMsg;

/**
 * 查单（渠道侧）接口定义
 *
 * @date 2021/5/19 15:16
 */
public interface IPayOrderQueryService {

    /**
     * 获取到接口code
     **/
    String getIfCode();

    /**
     * 查询订单
     **/
    ChannelRetMsg query(PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception;

    ChannelRetMsg queryTelegramBot(PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception;
}
