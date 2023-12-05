package com.test.channel;

import com.jeequan.jeepay.core.entity.MchDivisionReceiver;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayOrderDivisionRecord;
import com.test.model.MchAppConfigContext;
import com.test.rqrs.msg.ChannelRetMsg;

import java.util.HashMap;
import java.util.List;

/**
 * 分账接口
 *
 * @date 2021/8/22 08:59
 */
public interface IDivisionService {

    /**
     * 获取到接口code
     **/
    String getIfCode();

    /**
     * 是否支持该分账
     */
    boolean isSupport();

    /**
     * 绑定关系
     **/
    ChannelRetMsg bind(MchDivisionReceiver mchDivisionReceiver, MchAppConfigContext mchAppConfigContext);

    /**
     * 单次分账 （无需调用完结接口，或自动解冻商户资金)
     **/
    ChannelRetMsg singleDivision(PayOrder payOrder, List<PayOrderDivisionRecord> recordList, MchAppConfigContext mchAppConfigContext);

    /**
     * 查询分账结果
     **/
    HashMap<Long, ChannelRetMsg> queryDivision(PayOrder payOrder, List<PayOrderDivisionRecord> recordList, MchAppConfigContext mchAppConfigContext);

}
