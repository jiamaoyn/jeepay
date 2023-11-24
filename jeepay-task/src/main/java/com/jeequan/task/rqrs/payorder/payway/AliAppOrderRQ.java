package com.jeequan.task.rqrs.payorder.payway;

import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.task.rqrs.payorder.UnifiedOrderRQ;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/*
 * 支付方式： ALI_APP
 * @date 2021/6/8 17:34
 */
@Data
public class AliAppOrderRQ extends UnifiedOrderRQ {

    /**
     * 支付宝用户ID
     **/
    @NotBlank(message = "用户ID不能为空")
    private String buyerOpenId;

    /**
     * 构造函数
     **/
    public AliAppOrderRQ() {
        this.setWayCode(CS.PAY_WAY_CODE.ALI_APP); //默认 wayCode, 避免validate出现问题
    }

    @Override
    public String getChannelUserId() {
        return this.buyerOpenId;
    }
}
