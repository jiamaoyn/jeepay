package com.jeequan.jeepay.pay.rqrs.payorder.payway;

import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/*
 * 支付方式： WX_BAR
 *
 * @author zhuxiao
 * @site https://www.jeequan.com
 * @date 2021/6/8 17:34
 */
@Data
public class WxBarOrderRQ extends UnifiedOrderRQ {

    /**
     * 用户 支付条码
     **/
    @NotBlank(message = "支付条码不能为空")
    private String authCode;

    /**
     * 构造函数
     **/
    public WxBarOrderRQ() {
        this.setWayCode(CS.PAY_WAY_CODE.WX_BAR); //默认 wx_bar, 避免validate出现问题
    }

}
