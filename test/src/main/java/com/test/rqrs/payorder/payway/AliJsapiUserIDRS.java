package com.test.rqrs.payorder.payway;

import com.test.rqrs.AbstractRS;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class AliJsapiUserIDRS extends AbstractRS {
    /**
     * 支付参数
     **/
    private String userId;

    /**
     * 渠道返回错误代码
     **/
    private String errCode;

    /**
     * 渠道返回错误信息
     **/
    private String errMsg;
}
