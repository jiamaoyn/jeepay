package com.jeequan.jeepay.pay.rqrs;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AbstractMchRQ extends AbstractRQ {

    /**
     * 商户号
     **/
    @NotBlank(message = "商户号不能为空")
    private String mchNo;


}