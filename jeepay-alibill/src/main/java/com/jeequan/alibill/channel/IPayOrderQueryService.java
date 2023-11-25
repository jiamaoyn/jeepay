package com.jeequan.alibill.channel;

import com.alipay.api.domain.AccountLogItemResult;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.alibill.model.MchAppConfigContext;
import com.jeequan.alibill.rqrs.msg.ChannelRetMsg;

import java.util.Date;
import java.util.List;

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
    List<AccountLogItemResult> query(MchAppConfigContext mchAppConfigContext, Date startDate, Date endDate) throws Exception;
}
