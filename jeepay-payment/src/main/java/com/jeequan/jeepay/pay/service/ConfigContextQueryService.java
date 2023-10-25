package com.jeequan.jeepay.pay.service;

import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchApp;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.entity.PayInterfaceConfig;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.params.IsvParams;
import com.jeequan.jeepay.core.model.params.IsvsubMchParams;
import com.jeequan.jeepay.core.model.params.NormalMchParams;
import com.jeequan.jeepay.core.model.params.alipay.AlipayIsvParams;
import com.jeequan.jeepay.core.model.params.alipay.AlipayNormalMchParams;
import com.jeequan.jeepay.core.model.params.pppay.PpPayNormalMchParams;
import com.jeequan.jeepay.core.model.params.wxpay.WxpayIsvParams;
import com.jeequan.jeepay.core.model.params.wxpay.WxpayNormalMchParams;
import com.jeequan.jeepay.pay.model.*;
import com.jeequan.jeepay.service.impl.MchAppService;
import com.jeequan.jeepay.service.impl.MchInfoService;
import com.jeequan.jeepay.service.impl.PayInterfaceConfigService;
import com.jeequan.jeepay.service.impl.SysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/*
 * 配置信息查询服务 （兼容 缓存 和 直接查询方式）
 * @date 2021/11/18 14:41
 */
@Slf4j
@Service
public class ConfigContextQueryService {

    public final ConfigContextService configContextService;
    private final MchInfoService mchInfoService;
    private final MchAppService mchAppService;
    private final PayInterfaceConfigService payInterfaceConfigService;
    private final StringRedisTemplate stringRedisTemplate;

    public ConfigContextQueryService(ConfigContextService configContextService, MchInfoService mchInfoService, MchAppService mchAppService, PayInterfaceConfigService payInterfaceConfigService, StringRedisTemplate stringRedisTemplate) {
        this.configContextService = configContextService;
        this.mchInfoService = mchInfoService;
        this.mchAppService = mchAppService;
        this.payInterfaceConfigService = payInterfaceConfigService;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    private boolean isCache() {
        return SysConfigService.IS_USE_CACHE;
    }

    public MchApp queryMchApp(String mchNo, String mchAppId) {

        if (isCache()) {
            return configContextService.getMchAppConfigContext(mchNo, mchAppId).getMchApp();
        }

        return mchAppService.getOneByMch(mchNo, mchAppId);
    }
    public MchInfo queryMchInfo(String mchNo) {

        return mchInfoService.getOneByMch(mchNo);
    }

    public MchAppConfigContext queryMchInfoAndAppInfo(String mchAppId) {
        return queryMchInfoAndAppInfo(mchAppService.getById(mchAppId).getMchNo(), mchAppId);
    }

    public MchAppConfigContext queryMchInfoAndAppInfo(String mchNo, String mchAppId) {

        if (isCache()) {
            return configContextService.getMchAppConfigContext(mchNo, mchAppId);
        }

        MchInfo mchInfo = mchInfoService.getById(mchNo);
        MchApp mchApp = queryMchApp(mchNo, mchAppId);

        if (mchInfo == null || mchApp == null) {
            return null;
        }

        MchAppConfigContext result = new MchAppConfigContext();
        result.setMchInfo(mchInfo);
        result.setMchNo(mchNo);
        result.setMchType(mchInfo.getType());

        result.setMchApp(mchApp);
        result.setAppId(mchAppId);

        return result;
    }

    public MchAppConfigContext queryMchInfoAndAppInfoByPayCode(String mchNo, String wayCode) {
        List<MchApp> mchAppList = new ArrayList<>();
        mchAppService.list(MchApp.gw().eq(MchApp::getMchNo, mchNo)).forEach(mchApp -> {
            PayInterfaceConfig payInterfaceConfig = payInterfaceConfigService.getOne(PayInterfaceConfig.gw()
                    .select(PayInterfaceConfig::getIfCode, PayInterfaceConfig::getIfParams)
                    .eq(PayInterfaceConfig::getState, CS.YES)
                    .eq(PayInterfaceConfig::getInfoType, CS.INFO_TYPE_MCH_APP)
                    .eq(PayInterfaceConfig::getInfoId, mchNo)
                    .eq(PayInterfaceConfig::getIfCode, wayCode)
            );
            if (payInterfaceConfig != null) {
                mchAppList.add(mchApp);
            }

        });

        if (mchAppList.isEmpty()) {
            return null;
        }
        String string = stringRedisTemplate.opsForValue().get(mchNo+wayCode);
        int stringKey = 0;
        if (string != null){
            stringKey = Integer.parseInt(string);
            if (mchAppList.size()<stringKey+1){
                stringKey = 0;
            }
            stringRedisTemplate.opsForValue().set(mchNo, String.valueOf(stringKey+1), Duration.ofSeconds(60));
        }
        return queryMchInfoAndAppInfo(mchNo, mchAppList.get(stringKey).getAppId());
    }

    public MchAppConfigContext queryMchInfoAndAppInfoByMchNo(String mchNo) {

        MchInfo mchInfo = mchInfoService.getById(mchNo);

        if (mchInfo == null) {
            return null;
        }

        MchAppConfigContext result = new MchAppConfigContext();
        result.setMchInfo(mchInfo);
        result.setMchNo(mchNo);
        result.setMchType(mchInfo.getType());

        return result;
    }


    public NormalMchParams queryNormalMchParams(String mchNo, String mchAppId, String ifCode) {

        if (isCache()) {
            return configContextService.getMchAppConfigContext(mchNo, mchAppId).getNormalMchParamsByIfCode(ifCode);
        }

        // 查询商户的所有支持的参数配置
        PayInterfaceConfig payInterfaceConfig = payInterfaceConfigService.getOne(PayInterfaceConfig.gw()
                .select(PayInterfaceConfig::getIfCode, PayInterfaceConfig::getIfParams)
                .eq(PayInterfaceConfig::getState, CS.YES)
                .eq(PayInterfaceConfig::getInfoType, CS.INFO_TYPE_MCH_APP)
                .eq(PayInterfaceConfig::getInfoId, mchAppId)
                .eq(PayInterfaceConfig::getIfCode, ifCode)
        );
        if (payInterfaceConfig == null) {
            return null;
        }

        return NormalMchParams.factory(payInterfaceConfig.getIfCode(), payInterfaceConfig.getIfParams());
    }


    public IsvsubMchParams queryIsvsubMchParams(String mchNo, String mchAppId, String ifCode) {

        if (isCache()) {
            return configContextService.getMchAppConfigContext(mchNo, mchAppId).getIsvsubMchParamsByIfCode(ifCode);
        }

        // 查询商户的所有支持的参数配置
        PayInterfaceConfig payInterfaceConfig = payInterfaceConfigService.getOne(PayInterfaceConfig.gw()
                .select(PayInterfaceConfig::getIfCode, PayInterfaceConfig::getIfParams)
                .eq(PayInterfaceConfig::getState, CS.YES)
                .eq(PayInterfaceConfig::getInfoType, CS.INFO_TYPE_MCH_APP)
                .eq(PayInterfaceConfig::getInfoId, mchAppId)
                .eq(PayInterfaceConfig::getIfCode, ifCode)
        );

        if (payInterfaceConfig == null) {
            return null;
        }

        return IsvsubMchParams.factory(payInterfaceConfig.getIfCode(), payInterfaceConfig.getIfParams());
    }


    public IsvParams queryIsvParams(String isvNo, String ifCode) {

        if (isCache()) {
            IsvConfigContext isvConfigContext = configContextService.getIsvConfigContext(isvNo);
            return isvConfigContext == null ? null : isvConfigContext.getIsvParamsByIfCode(ifCode);
        }

        // 查询商户的所有支持的参数配置
        PayInterfaceConfig payInterfaceConfig = payInterfaceConfigService.getOne(PayInterfaceConfig.gw()
                .select(PayInterfaceConfig::getIfCode, PayInterfaceConfig::getIfParams)
                .eq(PayInterfaceConfig::getState, CS.YES)
                .eq(PayInterfaceConfig::getInfoType, CS.INFO_TYPE_ISV)
                .eq(PayInterfaceConfig::getInfoId, isvNo)
                .eq(PayInterfaceConfig::getIfCode, ifCode)
        );

        if (payInterfaceConfig == null) {
            return null;
        }

        return IsvParams.factory(payInterfaceConfig.getIfCode(), payInterfaceConfig.getIfParams());

    }

    public AlipayClientWrapper getAlipayClientWrapper(MchAppConfigContext mchAppConfigContext) {

        if (isCache()) {
            return
                    configContextService.getMchAppConfigContext(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId()).getAlipayClientWrapper();
        }

        if (mchAppConfigContext.isIsvsubMch()) {
            AlipayIsvParams alipayParams = (AlipayIsvParams) queryIsvParams(mchAppConfigContext.getMchInfo().getIsvNo(), CS.IF_CODE.ALIPAY);
            return AlipayClientWrapper.buildAlipayClientWrapper(alipayParams);
        } else {
            AlipayNormalMchParams alipayParams = (AlipayNormalMchParams) queryNormalMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), CS.IF_CODE.ALIPAY);
            System.out.println(alipayParams.getAppId());
            System.out.println(alipayParams.getAlipayPublicKey());
            System.out.println(alipayParams.getPrivateKey());
            return AlipayClientWrapper.buildAlipayClientWrapper(alipayParams);
        }

    }

    public WxServiceWrapper getWxServiceWrapper(MchAppConfigContext mchAppConfigContext) {

        if (isCache()) {
            return
                    configContextService.getMchAppConfigContext(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId()).getWxServiceWrapper();
        }

        if (mchAppConfigContext.isIsvsubMch()) {

            WxpayIsvParams wxParams = (WxpayIsvParams) queryIsvParams(mchAppConfigContext.getMchInfo().getIsvNo(), CS.IF_CODE.WXPAY);
            return WxServiceWrapper.buildWxServiceWrapper(wxParams);
        } else {

            WxpayNormalMchParams wxParams = (WxpayNormalMchParams) queryNormalMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), CS.IF_CODE.WXPAY);
            return WxServiceWrapper.buildWxServiceWrapper(wxParams);
        }

    }

    public PaypalWrapper getPaypalWrapper(MchAppConfigContext mchAppConfigContext) {
        if (isCache()) {
            return
                    configContextService.getMchAppConfigContext(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId()).getPaypalWrapper();
        }
        PpPayNormalMchParams ppPayNormalMchParams = (PpPayNormalMchParams) queryNormalMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), CS.IF_CODE.PPPAY);
        ;
        return PaypalWrapper.buildPaypalWrapper(ppPayNormalMchParams);

    }

}
