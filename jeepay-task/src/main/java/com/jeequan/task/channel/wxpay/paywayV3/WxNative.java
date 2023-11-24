package com.jeequan.task.channel.wxpay.paywayV3;

import com.alibaba.fastjson.JSONObject;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.task.channel.wxpay.WxpayPaymentService;
import com.jeequan.task.channel.wxpay.kits.WxpayKit;
import com.jeequan.task.channel.wxpay.kits.WxpayV3Util;
import com.jeequan.task.channel.wxpay.model.WxpayV3OrderRequestModel;
import com.jeequan.task.model.MchAppConfigContext;
import com.jeequan.task.model.WxServiceWrapper;
import com.jeequan.task.rqrs.AbstractRS;
import com.jeequan.task.rqrs.msg.ChannelRetMsg;
import com.jeequan.task.rqrs.payorder.UnifiedOrderRQ;
import com.jeequan.task.rqrs.payorder.payway.WxNativeOrderRQ;
import com.jeequan.task.rqrs.payorder.payway.WxNativeOrderRS;
import com.jeequan.task.util.ApiResBuilder;
import org.springframework.stereotype.Service;

/*
 * 微信 native支付
 *
 * @author zhuxiao
 * @site https://www.jeequan.com
 * @date 2021/6/8 18:08
 */
@Service("wxpayPaymentByNativeV3Service") //Service Name需保持全局唯一性
public class WxNative extends WxpayPaymentService {

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {
        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) {

        WxNativeOrderRQ bizRQ = (WxNativeOrderRQ) rq;

        WxServiceWrapper wxServiceWrapper = configContextQueryService.getWxServiceWrapper(mchAppConfigContext);

        WxPayService wxPayService = wxServiceWrapper.getWxPayService();

        // 构造请求数据
        WxpayV3OrderRequestModel wxpayV3OrderRequestModel = buildV3OrderRequestModel(payOrder, mchAppConfigContext);

        // 构造函数响应数据
        WxNativeOrderRS res = ApiResBuilder.buildSuccess(WxNativeOrderRS.class);
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        res.setChannelRetMsg(channelRetMsg);

        // 调起上游接口：
        try {
            String payInfo = WxpayV3Util.commonReqWx(wxpayV3OrderRequestModel, wxPayService, mchAppConfigContext.isIsvsubMch(), WxPayConstants.TradeType.NATIVE, null);

            JSONObject resJSON = JSONObject.parseObject(payInfo);

            String codeUrl = resJSON.getString("code_url");
            if (CS.PAY_DATA_TYPE.CODE_IMG_URL.equals(bizRQ.getPayDataType())) { //二维码图片地址

                res.setCodeImgUrl(sysConfigService.getDBApplicationConfig().genScanImgUrl(codeUrl));
            } else {

                res.setCodeUrl(codeUrl);
            }

            // 支付中
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);

        } catch (WxPayException e) {
            //明确失败
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
            WxpayKit.commonSetErrInfo(channelRetMsg, e);
        }

        return res;
    }

}
