package com.jeequan.task.channel;


import com.jeequan.task.service.ConfigContextQueryService;
import com.jeequan.task.util.ChannelCertConfigKitBean;
import com.jeequan.jeepay.service.impl.SysConfigService;
import org.springframework.beans.factory.annotation.Autowired;

/*
 * 退款接口抽象类
 * @date 2021/6/17 9:37
 */
public abstract class AbstractRefundService implements IRefundService {

    @Autowired
    protected SysConfigService sysConfigService;
    @Autowired
    protected ChannelCertConfigKitBean channelCertConfigKitBean;
    @Autowired
    protected ConfigContextQueryService configContextQueryService;

    protected String getNotifyUrl() {
        return sysConfigService.getDBApplicationConfig().getPaySiteUrl() + "/api/refund/notify/" + getIfCode();
    }

    protected String getNotifyUrl(String refundOrderId) {
        return sysConfigService.getDBApplicationConfig().getPaySiteUrl() + "/api/refund/notify/" + getIfCode() + "/" + refundOrderId;
    }

}
