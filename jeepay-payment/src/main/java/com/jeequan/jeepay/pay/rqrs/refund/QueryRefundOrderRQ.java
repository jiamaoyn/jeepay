package com.jeequan.jeepay.pay.rqrs.refund;

import com.jeequan.jeepay.pay.rqrs.AbstractMchAppRQ;
import lombok.Data;
import lombok.EqualsAndHashCode;

/*
 * 查询退款单请求参数对象
 * @date 2021/6/17 14:07
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QueryRefundOrderRQ extends AbstractMchAppRQ {

    /**
     * 商户退款单号
     **/
    private String mchRefundNo;

    /**
     * 支付系统退款订单号
     **/
    private String refundOrderId;

}
