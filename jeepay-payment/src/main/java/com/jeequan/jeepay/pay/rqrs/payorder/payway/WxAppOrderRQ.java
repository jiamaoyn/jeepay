package com.jeequan.jeepay.pay.rqrs.payorder.payway;

import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/*
 *  支付方式： WX_APP
 *
 * @author xiaoyu
 * @site https://www.jeequan.com
 * @date 2022/12/20 8:12
 */
@Data
public class WxAppOrderRQ extends UnifiedOrderRQ {

    /**
     * 微信openid
     **/
    @NotBlank(message = "openid不能为空")
    private String openid;

    /**
     * 构造函数
     **/
    public WxAppOrderRQ() {
        this.setWayCode(CS.PAY_DATA_TYPE.WX_APP); //默认 wayCode, 避免validate出现问题
    }


    @Override
    public String getChannelUserId() {
        return this.openid;
    }
}
