package com.test.rqrs.payorder.payway;

import com.jeequan.jeepay.core.constants.CS;
import com.test.rqrs.payorder.UnifiedOrderRQ;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/*
 * 支付方式： ALI_JSAPI
 * @date 2021/6/8 17:34
 */
@Data
public class AliJsapiOrderRQ extends UnifiedOrderRQ {

    /**
     * 支付宝用户ID
     **/
    @NotBlank(message = "用户ID不能为空")
    private String buyerOpenId;

    /**
     * 构造函数
     **/
    public AliJsapiOrderRQ() {
        this.setWayCode(CS.PAY_WAY_CODE.ALI_JSAPI);
    }

    @Override
    public String getChannelUserId() {
        return this.buyerOpenId;
    }

}
