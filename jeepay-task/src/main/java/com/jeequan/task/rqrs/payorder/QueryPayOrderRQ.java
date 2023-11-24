package com.jeequan.task.rqrs.payorder;

import com.jeequan.task.rqrs.AbstractMchAppRQ;
import lombok.Data;

/*
 * 查询订单请求参数对象
 * @date 2021/6/8 17:40
 */
@Data
public class QueryPayOrderRQ extends AbstractMchAppRQ {

    /**
     * 商户订单号
     **/
    private String mchOrderNo;

    /**
     * 支付系统订单号
     **/
    private String payOrderId;

}
