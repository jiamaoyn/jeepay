package com.jeequan.jeepay.pay.ctrl;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.ctrls.AbstractCtrl;
import com.jeequan.jeepay.core.entity.MchApp;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.utils.JeepayKit;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.AbstractMchAppRQ;
import com.jeequan.jeepay.pay.rqrs.AbstractRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.AliJsapiUserIDRQ;
import com.jeequan.jeepay.pay.service.ConfigContextQueryService;
import com.jeequan.jeepay.pay.service.ValidateService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/*
 * api 抽象接口， 公共函数
 * @date 2021/6/8 17:28
 */
public abstract class ApiController extends AbstractCtrl {

    @Autowired
    private ValidateService validateService;
    @Autowired
    private ConfigContextQueryService configContextQueryService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    /**
     * 获取请求参数并转换为对象，通用验证
     **/
    protected <T extends AbstractRQ> T getRQ(Class<T> cls) {

        T bizRQ = getObject(cls);

        // [1]. 验证通用字段规则
        validateService.validate(bizRQ);

        return bizRQ;
    }


    /**
     * 获取请求参数并转换为对象，商户通用验证
     **/
    protected <T extends AbstractRQ> T getRQByWithMchSign(Class<T> cls) {

        //获取请求RQ, and 通用验证
        T bizRQ = getRQ(cls);

        AbstractMchAppRQ abstractMchAppRQ = (AbstractMchAppRQ) bizRQ;

        //业务校验， 包括： 验签， 商户状态是否可用， 是否支持该支付方式下单等。
        String mchNo = abstractMchAppRQ.getMchNo();
        String appId = abstractMchAppRQ.getAppId();
        String sign = bizRQ.getSign();

        if (StringUtils.isAnyBlank(mchNo, appId, sign)) {
            throw new BizException("参数有误！");
        }

        MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(mchNo, appId);

        if (mchAppConfigContext == null) {
            throw new BizException("商户或商户应用不存在");
        }

        if (mchAppConfigContext.getMchInfo() == null || mchAppConfigContext.getMchInfo().getState() != CS.YES) {
            throw new BizException("商户信息不存在或商户状态不可用");
        }

        MchApp mchApp = mchAppConfigContext.getMchApp();
        if (mchApp == null || mchApp.getState() != CS.YES) {
            throw new BizException("商户应用不存在或应用状态不可用");
        }

        if (!mchApp.getMchNo().equals(mchNo)) {
            throw new BizException("参数appId与商户号不匹配");
        }

        // 验签
        String appSecret = mchApp.getAppSecret();
        System.out.println(appSecret);
        // 转换为 JSON
        JSONObject bizReqJSON = (JSONObject) JSONObject.toJSON(bizRQ);
        bizReqJSON.remove("sign");
        if (!sign.equalsIgnoreCase(JeepayKit.getSign(bizReqJSON, appSecret))) {
            throw new BizException("验签失败");
        }
        if (mchApp.getSpeed()!=0) {
            String redisKey = mchNo+appId;
            String string = stringRedisTemplate.opsForValue().get(redisKey);
            if (string != null){
                if (mchApp.getSpeed()<Integer.parseInt(string)+1){
                    throw new BizException("已超出接口配置速率，请60s后重试或商户后台修改速率");
                } else {
                    // 获取原始键的剩余过期时间（秒）
                    Long ttl = stringRedisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
                    if (ttl != null && ttl > 0) {
                        stringRedisTemplate.opsForValue().set(redisKey, String.valueOf(Integer.parseInt(string)+1));
                        // 设置新的过期时间
                        stringRedisTemplate.expire(redisKey, ttl, TimeUnit.SECONDS);
                    } else {
                        stringRedisTemplate.opsForValue().set(redisKey, "1", Duration.ofSeconds(60));
                    }
                }
            } else {
                stringRedisTemplate.opsForValue().set(redisKey, "1", Duration.ofSeconds(60));
            }
        }
        return bizRQ;
    }
    protected <T extends AbstractRQ> MchAppConfigContext getRQByWithMchSignAliJsapiUserId(AliJsapiUserIDRQ abstractMchAppRQ) {

        //业务校验， 包括： 验签， 商户状态是否可用， 是否支持该支付方式下单等。
        String mchNo = abstractMchAppRQ.getMchNo();
        String appId = abstractMchAppRQ.getAppId();
        String sign = abstractMchAppRQ.getSign();

        if (StringUtils.isAnyBlank(mchNo, appId, sign)) {
            throw new BizException("参数有误！");
        }

        MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(mchNo, appId);

        if (mchAppConfigContext == null) {
            throw new BizException("商户或商户应用不存在");
        }

        if (mchAppConfigContext.getMchInfo() == null || mchAppConfigContext.getMchInfo().getState() != CS.YES) {
            throw new BizException("商户信息不存在或商户状态不可用");
        }

        MchApp mchApp = mchAppConfigContext.getMchApp();
        if (mchApp == null || mchApp.getState() != CS.YES) {
            throw new BizException("商户应用不存在或应用状态不可用");
        }

        if (!mchApp.getMchNo().equals(mchNo)) {
            throw new BizException("参数appId与商户号不匹配");
        }
        return mchAppConfigContext;
    }
    protected <T extends AbstractRQ> T getRQByWithMchSignBot(Class<T> cls) {
        //获取请求RQ, and 通用验证
        T bizRQ = getRQ(cls);

        AbstractMchAppRQ abstractMchAppRQ = (AbstractMchAppRQ) bizRQ;

        //业务校验， 包括： 验签， 商户状态是否可用， 是否支持该支付方式下单等。
        String mchNo = abstractMchAppRQ.getMchNo();
        String sign = bizRQ.getSign();

        if (StringUtils.isAnyBlank(mchNo, sign)) {
            throw new BizException("参数有误！");
        }

        MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfoByMchNo(mchNo);

        if (mchAppConfigContext == null) {
            throw new BizException("商户或商户应用不存在");
        }

        if (mchAppConfigContext.getMchInfo() == null || mchAppConfigContext.getMchInfo().getState() != CS.YES) {
            throw new BizException("商户信息不存在或商户状态不可用");
        }

        // 验签
        String appSecret = "tdjzfs0tsjcuj2qwu2lle88slfhj3jtxu5t5ut9sdb2tds9ajg7gy37rsajm05fkqaxa2rvjoqkso8wwh3guv5o1fedjacndfclvnya2f8rz57rff7gpup0eepvg0zsp";
        // 转换为 JSON
        JSONObject bizReqJSON = (JSONObject) JSONObject.toJSON(bizRQ);
        bizReqJSON.remove("sign");
        if (!sign.equalsIgnoreCase(JeepayKit.getSign(bizReqJSON, appSecret))) {
            throw new BizException("验签失败");
        }
        return bizRQ;
    }
}
