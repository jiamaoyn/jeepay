package com.jeepay.telegram.model;

import com.jeequan.jeepay.core.entity.MchApp;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.model.params.IsvsubMchParams;
import com.jeequan.jeepay.core.model.params.NormalMchParams;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/*
 * 商户应用支付参数信息
 * 放置到内存， 避免多次查询操作
 * @date 2021/6/8 17:29
 */
@Data
public class MchAppConfigContext {


    /**
     * 商户信息缓存
     */
    private String mchNo;
    private String appId;
    private Byte mchType;
    private MchInfo mchInfo;
    private MchApp mchApp;

    /**
     * 商户支付配置信息缓存,  <接口代码, 支付参数>
     */
    private Map<String, NormalMchParams> normalMchParamsMap = new HashMap<>();
    private Map<String, IsvsubMchParams> isvsubMchParamsMap = new HashMap<>();

    /**
     * 放置所属服务商的信息
     **/
    private IsvConfigContext isvConfigContext;

    /**
     * 缓存支付宝client 对象
     **/
    private AlipayClientWrapper alipayClientWrapper;


    /**
     * 获取普通商户配置信息
     **/
    public NormalMchParams getNormalMchParamsByIfCode(String ifCode) {
        return normalMchParamsMap.get(ifCode);
    }

    /**
     * 获取isv配置信息
     **/
    public <T> T getNormalMchParamsByIfCode(String ifCode, Class<? extends NormalMchParams> cls) {
        return (T) normalMchParamsMap.get(ifCode);
    }

    /**
     * 获取特约商户配置信息
     **/
    public IsvsubMchParams getIsvsubMchParamsByIfCode(String ifCode) {
        return isvsubMchParamsMap.get(ifCode);
    }

    /**
     * 是否为 服务商特约商户
     **/
    public boolean isIsvsubMch() {
        return this.mchType == MchInfo.TYPE_ISVSUB;
    }

    public AlipayClientWrapper getAlipayClientWrapper() {
        return isIsvsubMch() ? isvConfigContext.getAlipayClientWrapper() : alipayClientWrapper;
    }

}
