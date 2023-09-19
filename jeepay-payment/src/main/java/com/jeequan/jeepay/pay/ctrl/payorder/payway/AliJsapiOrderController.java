package com.jeequan.jeepay.pay.ctrl.payorder.payway;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.request.AlipayUserInfoShareRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.alipay.api.response.AlipayTradeCreateResponse;
import com.alipay.api.response.AlipayUserInfoShareResponse;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchApp;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.model.params.NormalMchParams;
import com.jeequan.jeepay.core.model.params.alipay.AlipayNormalMchParams;
import com.jeequan.jeepay.pay.ctrl.payorder.AbstractPayOrderController;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRS;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.AliJsapiOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.AliJsapiUserIDRQ;
import com.jeequan.jeepay.pay.service.ConfigContextQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/*
 * 支付宝 jspai controller
 * @date 2021/6/8 17:25
 */
@Slf4j
@RestController
public class AliJsapiOrderController extends AbstractPayOrderController {
    @Autowired
    protected ConfigContextQueryService configContextQueryService;

    /**
     * 统一下单接口
     **/
    @PostMapping("/api/pay/aliJsapiOrder")
    public ApiRes aliJsapiOrder() {

        //获取参数 & 验证
        AliJsapiOrderRQ bizRQ = getRQByWithMchSign(AliJsapiOrderRQ.class);

        // 统一下单接口
        return unifiedOrder(CS.PAY_WAY_CODE.ALI_JSAPI, bizRQ);

    }

    @PostMapping("/api/pay/aliJsapiOrderToUserId")
    public ApiRes<String> aliJsapiOrderToUserId() {
        ApiRes<String> apiRes = new ApiRes<>();
        AliJsapiUserIDRQ bizRQ = getRQ(AliJsapiUserIDRQ.class);
        //获取参数 & 验证
        MchAppConfigContext mchAppConfigContext = getRQByWithMchSignAliJsapiUserId(bizRQ);
        AlipayNormalMchParams alipayParams = (AlipayNormalMchParams) configContextQueryService.queryNormalMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), "alipay");
        System.out.println(bizRQ);
        apiRes.setCode(10000);
        try {
            AlipayConfig alipayConfig = new AlipayConfig();
            alipayConfig.setServerUrl("https://openapi-sandbox.dl.alipaydev.com/gateway.do");
            alipayConfig.setAppId(alipayParams.getAppId());
            alipayConfig.setPrivateKey(alipayParams.getPrivateKey());
            alipayConfig.setFormat("json");
            alipayConfig.setAlipayPublicKey(alipayParams.getAlipayPublicKey());
            alipayConfig.setCharset("UTF8");
            alipayConfig.setSignType("RSA2");
            AlipayClient alipayClient = new DefaultAlipayClient(alipayConfig);
            AlipaySystemOauthTokenRequest request = new AlipaySystemOauthTokenRequest();
            request.setCode(bizRQ.getAuthToken());
            request.setGrantType("authorization_code");
            AlipaySystemOauthTokenResponse response = alipayClient.execute(request);
            if(response.isSuccess()){
                apiRes.setCode(0);
                apiRes.setMsg("调用成功");
                apiRes.setData(response.getUserId());
            } else {
                apiRes.setMsg(response.getSubMsg());
            }
        } catch (AlipayApiException e) {
            apiRes.setMsg(e.getMessage()+e.getErrMsg());
        }
        return apiRes;
    }


}
