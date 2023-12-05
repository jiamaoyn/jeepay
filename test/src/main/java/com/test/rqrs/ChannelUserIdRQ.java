package com.test.rqrs;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

/*
 * 商户获取渠道用户ID 请求参数对象
 * @date 2021/6/8 17:40
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChannelUserIdRQ extends AbstractMchAppRQ {

    /**
     * 接口代码,  AUTO表示：自动获取
     **/
    @NotBlank(message = "接口代码不能为空")
    private String ifCode;

    /**
     * 商户扩展参数，将原样返回
     **/
    private String extParam;

    /**
     * 回调地址
     **/
    @NotBlank(message = "回调地址不能为空")
    private String redirectUrl;

}
