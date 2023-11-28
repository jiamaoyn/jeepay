package com.jeepay.telegram.rqrs;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/*
 *
 * 通用RQ, 包含mchNo和appId 必填项
 * @date 2021/6/16 10:30
 */
@Data
public class AbstractMchAppRQ extends AbstractRQ {

    /**
     * 商户号
     **/
    @NotBlank(message = "商户号不能为空")
    private String mchNo;

    /**
     * 商户应用ID
     **/
    private String appId;


}
