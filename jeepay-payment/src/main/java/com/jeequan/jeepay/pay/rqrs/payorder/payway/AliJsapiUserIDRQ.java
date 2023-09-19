package com.jeequan.jeepay.pay.rqrs.payorder.payway;

import com.jeequan.jeepay.pay.rqrs.AbstractMchAppRQ;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

@EqualsAndHashCode(callSuper = true)
@Data
public class AliJsapiUserIDRQ extends AbstractMchAppRQ {
    /**
     * 支付宝用户ID
     **/
    @NotBlank(message = "Token不能为空")
    private String authToken;
}
