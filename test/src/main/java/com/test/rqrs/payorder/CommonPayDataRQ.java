package com.test.rqrs.payorder;

import lombok.Data;
import lombok.EqualsAndHashCode;

/*
 * 通用支付数据RQ
 * @date 2021/6/8 17:31
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CommonPayDataRQ extends UnifiedOrderRQ {

    /**
     * 请求参数： 支付数据包类型
     **/
    private String payDataType;

}
