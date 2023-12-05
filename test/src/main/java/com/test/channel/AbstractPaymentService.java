package com.test.channel;


import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.service.impl.SysConfigService;
import com.test.model.MchAppConfigContext;
import com.test.rqrs.payorder.UnifiedOrderRQ;
import com.test.service.ConfigContextQueryService;
import com.test.util.ChannelCertConfigKitBean;
import org.springframework.beans.factory.annotation.Autowired;

/*
 * 支付接口抽象类
 * @date 2021/6/8 17:18
 */
public abstract class AbstractPaymentService implements IPaymentService {

    @Autowired
    protected SysConfigService sysConfigService;
    @Autowired
    protected ChannelCertConfigKitBean channelCertConfigKitBean;
    @Autowired
    protected ConfigContextQueryService configContextQueryService;

    @Override
    public String customPayOrderId(UnifiedOrderRQ bizRQ, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) {
        return null; //使用系统默认支付订单号
    }

    /**
     * 订单分账（一般用作 如微信订单将在下单处做标记）
     */
    protected boolean isDivisionOrder(PayOrder payOrder) {
        //订单分账， 将冻结商户资金。
        return payOrder.getDivisionMode() != null && (PayOrder.DIVISION_MODE_AUTO == payOrder.getDivisionMode() || PayOrder.DIVISION_MODE_MANUAL == payOrder.getDivisionMode());
    }

    protected String getNotifyUrl() {
        return sysConfigService.getDBApplicationConfig().getPaySiteUrl() + "/api/pay/notify/" + getIfCode();
    }

    protected String getNotifyUrl(String payOrderId) {
        return sysConfigService.getDBApplicationConfig().getPaySiteUrl() + "/api/pay/notify/" + getIfCode() + "/" + payOrderId;
    }

    protected String getReturnUrl() {
        return sysConfigService.getDBApplicationConfig().getPaySiteUrl() + "/api/pay/return/" + getIfCode();
    }

    protected String getReturnUrl(String payOrderId) {
        return sysConfigService.getDBApplicationConfig().getPaySiteUrl() + "/api/pay/return/" + getIfCode() + "/" + payOrderId;
    }

    protected String getReturnUrlOnlyJump(String payOrderId) {
        return sysConfigService.getDBApplicationConfig().getPaySiteUrl() + "/api/pay/return/" + getIfCode() + "/" + CS.PAY_RETURNURL_FIX_ONLY_JUMP_PREFIX + payOrderId;
    }

}
