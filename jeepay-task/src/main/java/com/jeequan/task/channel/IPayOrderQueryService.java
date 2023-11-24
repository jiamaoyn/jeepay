package com.jeequan.task.channel;

import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.task.model.MchAppConfigContext;
import com.jeequan.task.rqrs.msg.ChannelRetMsg;

import java.util.Date;

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
    ChannelRetMsg query(PayOrder payOrder, MchAppConfigContext mchAppConfigContext, Date startDate, Date endDate) throws Exception;
}
