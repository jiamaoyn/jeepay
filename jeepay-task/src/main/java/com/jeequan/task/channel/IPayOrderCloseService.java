package com.jeequan.task.channel;

import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.task.model.MchAppConfigContext;
import com.jeequan.task.rqrs.msg.ChannelRetMsg;

/**
 * 关闭订单（渠道侧）接口定义
 *
 * @author xiaoyu
 * @site https://www.jeequan.com
 * @date 2022/1/24 17:23
 */
public interface IPayOrderCloseService {

    /**
     * 获取到接口code
     **/
    String getIfCode();

    /**
     * 查询订单
     **/
    ChannelRetMsg close(PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception;

}
