package com.jeequan.task.rqrs.division;

import com.jeequan.task.rqrs.AbstractRS;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 发起订单分账 响应参数
 *
 * @date 2021/8/26 17:20
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PayOrderDivisionExecRS extends AbstractRS {

    /**
     * 分账状态 1-分账成功, 2-分账失败
     */
    private Byte state;

    /**
     * 上游分账批次号
     */
    private String channelBatchOrderId;

    /**
     * 支付渠道错误码
     */
    private String errCode;

    /**
     * 支付渠道错误信息
     */
    private String errMsg;


}
