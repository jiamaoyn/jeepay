package com.jeequan.alibill.model;

import com.jeequan.jeepay.core.entity.MchApp;
import com.jeequan.jeepay.core.entity.MchInfo;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 * 商户配置信息
 * 放置到内存， 避免多次查询操作
 * @date 2021/6/8 17:29
 */
@Data
public class MchInfoConfigContext {


    /**
     * 商户信息缓存
     */
    private String mchNo;
    private Byte mchType;
    private MchInfo mchInfo;
    private Map<String, MchApp> appMap = new ConcurrentHashMap<>();

    /**
     * 重置商户APP
     **/
    public void putMchApp(MchApp mchApp) {
        appMap.put(mchApp.getAppId(), mchApp);
    }

}
