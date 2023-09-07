package com.jeequan.jeepay.pay.rqrs.payorder;

import com.jeequan.jeepay.pay.rqrs.AbstractMchAppRQ;
import lombok.Data;

/*
 * 关闭订单 请求参数对象
 *
 * @author xiaoyu
 * @site https://www.jeequan.com
 * @date 2022/1/25 9:16
 */
@Data
public class ClosePayOrderRQ extends AbstractMchAppRQ {

    /**
     * 商户订单号
     **/
    private String mchOrderNo;

    /**
     * 支付系统订单号
     **/
    private String payOrderId;

}
