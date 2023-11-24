package com.jeequan.task.rqrs;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/*
 * 基础请求参数
 * @date 2021/6/8 17:39
 */
@Data
public abstract class AbstractRQ implements Serializable {

    /**
     * 版本号
     **/
    @NotBlank(message = "版本号不能为空")
    protected String version;

    /**
     * 签名类型
     **/
    @NotBlank(message = "签名类型不能为空")
    protected String signType;

    /**
     * 签名值
     **/
    @NotBlank(message = "签名值不能为空")
    protected String sign;

    /**
     * 接口请求时间
     **/
    @NotBlank(message = "时间戳不能为空")
    protected String reqTime;

}
