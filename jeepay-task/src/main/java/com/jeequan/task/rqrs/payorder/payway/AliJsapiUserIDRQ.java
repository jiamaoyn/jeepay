package com.jeequan.task.rqrs.payorder.payway;

import com.jeequan.task.rqrs.AbstractMchAppRQ;
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
