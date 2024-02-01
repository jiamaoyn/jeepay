package com.jeequan.jeepay.pay.ctrl.payorder;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.model.params.alipay.AlipayIsvsubMchParams;
import com.jeequan.jeepay.core.model.params.alipay.AlipayNormalMchParams;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.core.utils.SpringBeansUtil;
import com.jeequan.jeepay.pay.channel.IPayOrderQueryService;
import com.jeequan.jeepay.pay.channel.alipay.AlipayKit;
import com.jeequan.jeepay.pay.ctrl.ApiController;
import com.jeequan.jeepay.pay.exception.ChannelException;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.rqrs.payorder.QueryPayOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.QueryPayOrderRS;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.AliPcOrderRS;
import com.jeequan.jeepay.pay.service.ConfigContextQueryService;
import com.jeequan.jeepay.pay.util.ApiResBuilder;
import com.jeequan.jeepay.service.impl.PayOrderService;
import com.jeequan.jeepay.service.impl.SysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * 商户查单controller
 */
@Slf4j
@RestController
public class QueryOrderController extends ApiController {

    @Autowired
    private PayOrderService payOrderService;
    @Autowired
    private SysConfigService sysConfigService;
    @Autowired
    private ConfigContextQueryService configContextQueryService;

    /**
     * 查单接口
     **/
    @RequestMapping("/api/pay/query")
    public ApiRes queryOrder() {

        //获取参数 & 验签
        QueryPayOrderRQ rq = getRQByWithMchSign(QueryPayOrderRQ.class);

        if (StringUtils.isAllEmpty(rq.getMchOrderNo(), rq.getPayOrderId())) {
            throw new BizException("mchOrderNo 和 payOrderId不能同时为空");
        }

        PayOrder payOrder = payOrderService.queryMchOrder(rq.getMchNo(), rq.getPayOrderId(), rq.getMchOrderNo());
        if (payOrder == null) {
            throw new BizException("订单不存在");
        }

        QueryPayOrderRS bizRes = QueryPayOrderRS.buildByPayOrder(payOrder);
        return ApiRes.okWithSign(bizRes, configContextQueryService.queryMchApp(rq.getMchNo(), rq.getAppId()).getAppSecret());
    }
    @RequestMapping("/api/pay/queryPolling")
    public ApiRes queryOrderPolling() {

        //获取参数 & 验签
        QueryPayOrderRQ rq = getRQByWithMchSignPolling(QueryPayOrderRQ.class);

        if (StringUtils.isAllEmpty(rq.getMchOrderNo(), rq.getPayOrderId())) {
            throw new BizException("mchOrderNo 和 payOrderId不能同时为空");
        }

        PayOrder payOrder = payOrderService.queryMchOrder(rq.getMchNo(), rq.getPayOrderId(), rq.getMchOrderNo());
        if (payOrder == null) {
            throw new BizException("订单不存在");
        }

        QueryPayOrderRS bizRes = QueryPayOrderRS.buildByPayOrder(payOrder);
        return ApiRes.okWithSign(bizRes, configContextQueryService.queryMchInfo(rq.getMchNo()).getSecret());
    }

    @RequestMapping("/api/pay/bill/{payOrderId}")
    public void aliPayBill(@PathVariable("payOrderId") String payOrderId) throws IOException {

        PayOrder payOrder = payOrderService.queryMchOrder(payOrderId);
        if (payOrder == null) {
            throw new BizException("订单不存在");
        }
        //获取支付参数 (缓存数据) 和 商户信息
        MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(payOrder.getMchNo(), payOrder.getAppId());
        if (mchAppConfigContext == null) {
            throw new BizException("获取商户应用信息失败");
        }
        String pid;
        if (!mchAppConfigContext.isIsvsubMch()){
            AlipayNormalMchParams normalMchParams = (AlipayNormalMchParams) configContextQueryService.queryNormalMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), "alipay");
            if (normalMchParams == null) {
                throw new BizException("商户支付宝接口没有配置！");
            }
            pid = normalMchParams.getPid();
        } else {
            AlipayIsvsubMchParams normalMchParams = (AlipayIsvsubMchParams) configContextQueryService.queryIsvsubMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), "alipay");
            if (normalMchParams == null) {
                throw new BizException("商户支付宝接口没有配置！");
            }
            pid = normalMchParams.getUserId();
        }
        if (payOrder.getState() == PayOrder.STATE_INIT){
            boolean isSuccess = payOrderService.updateInit2Ing(payOrder.getPayOrderId(), payOrder);
            if (!isSuccess) {
                throw new BizException("更新订单异常!");
            }
        }
        response.sendRedirect("https://www.alipay.com/?appId=20000116&actionType=toAccount&sourceId=contactStage&chatUserId="+pid+"&displayName=TK&chatUserName=TK&chatUserType=1&skipAuth=true&amount="+ AmountUtil.convertCent2Dollar(payOrder.getAmount().toString())+"&memo="+payOrderId);
    }
    @RequestMapping("/api/pay/bill_q_pay/{payOrderId}")
    public ApiRes queryOrderBillTem(@PathVariable("payOrderId") String payOrderId) throws Exception {
        PayOrder payOrder = payOrderService.queryMchOrder(payOrderId);
        if (payOrder == null) {
            throw new BizException("订单不存在");
        }
        IPayOrderQueryService queryService = SpringBeansUtil.getBean(payOrder.getIfCode() + "PayOrderQueryService", IPayOrderQueryService.class);
        // 支付通道接口实现不存在
        if (queryService == null) {
            log.error("{} interface not exists!", payOrder.getIfCode());
            return null;
        }
        PayOrder payOrder1 = new PayOrder();
        if (payOrder.getState() == PayOrder.STATE_ING){
            MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(payOrder.getMchNo(), payOrder.getAppId());
            ChannelRetMsg channelRetMsg = queryService.query(payOrder, mchAppConfigContext);
            if (channelRetMsg.getChannelState() == ChannelRetMsg.ChannelState.CONFIRM_SUCCESS){
                payOrder1.setState(PayOrder.STATE_SUCCESS);
            }
        } else {
            payOrder1.setState(payOrder.getState());
        }
        //获取支付参数 (缓存数据) 和 商户信息
        MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(payOrder.getMchNo(), payOrder.getAppId());
        if (mchAppConfigContext == null) {
            throw new BizException("获取商户应用信息失败");
        }
        String pid;
        if (!mchAppConfigContext.isIsvsubMch()){
            AlipayNormalMchParams normalMchParams = (AlipayNormalMchParams) configContextQueryService.queryNormalMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), "alipay");
            if (normalMchParams == null) {
                throw new BizException("商户支付宝接口没有配置！");
            }
            pid = normalMchParams.getPid();
        } else {
            AlipayIsvsubMchParams normalMchParams = (AlipayIsvsubMchParams) configContextQueryService.queryIsvsubMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), "alipay");
            if (normalMchParams == null) {
                throw new BizException("商户支付宝接口没有配置！");
            }
            pid = normalMchParams.getUserId();
        }

        payOrder1.setReturnUrl(request.getScheme()+"://"+request.getServerName() + sysConfigService.getDBApplicationConfig().genScanImgUrlDomain("https://www.alipay.com/?appId=20000116&actionType=toAccount&sourceId=contactStage&chatUserId="+pid+"&displayName=TK&chatUserName=TK&chatUserType=1&skipAuth=true&amount="+ AmountUtil.convertCent2Dollar(payOrder.getAmount().toString())+"&memo="+payOrderId));
        return ApiRes.ok(payOrder1);
    }
    @RequestMapping("/api/pay/ali_pc_h5_q_pay/{payOrderId}")
    public String queryOrderAliPcH5(@PathVariable("payOrderId") String payOrderId) throws Exception {
        PayOrder payOrder = payOrderService.queryMchOrder(payOrderId);
        if (payOrder == null) {
            throw new BizException("订单不存在");
        }
        IPayOrderQueryService queryService = SpringBeansUtil.getBean(payOrder.getIfCode() + "PayOrderQueryService", IPayOrderQueryService.class);
        // 支付通道接口实现不存在
        if (queryService == null) {
            log.error("{} interface not exists!", payOrder.getIfCode());
            return null;
        }
        PayOrder payOrder1 = new PayOrder();
        if (payOrder.getState() == PayOrder.STATE_ING){
            MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(payOrder.getMchNo(), payOrder.getAppId());
            ChannelRetMsg channelRetMsg = queryService.query(payOrder, mchAppConfigContext);
            if (channelRetMsg.getChannelState() == ChannelRetMsg.ChannelState.CONFIRM_SUCCESS){
                payOrder1.setState(PayOrder.STATE_SUCCESS);
            }
        } else {
            payOrder1.setState(payOrder.getState());
        }
        //获取支付参数 (缓存数据) 和 商户信息
        MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(payOrder.getMchNo(), payOrder.getAppId());
        if (mchAppConfigContext == null) {
            throw new BizException("获取商户应用信息失败");
        }
        String formContent = "";
        if (!mchAppConfigContext.isIsvsubMch()){
            AlipayNormalMchParams normalMchParams = (AlipayNormalMchParams) configContextQueryService.queryNormalMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), "alipay");
            if (normalMchParams == null) {
                throw new BizException("商户支付宝接口没有配置！");
            }
        } else {
            AlipayIsvsubMchParams normalMchParams = (AlipayIsvsubMchParams) configContextQueryService.queryIsvsubMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), "alipay");
            if (normalMchParams == null) {
                throw new BizException("商户支付宝接口没有配置！");
            }
            AlipayTradePagePayRequest req = new AlipayTradePagePayRequest();
            AlipayTradePagePayModel model = new AlipayTradePagePayModel();
            model.setOutTradeNo(payOrder.getPayOrderId());
            model.setSubject(payOrder.getSubject()); //订单标题
            model.setBody(payOrder.getBody()); //订单描述信息
            model.setTotalAmount(AmountUtil.convertCent2Dollar(payOrder.getAmount().toString()));  //支付金额
            model.setTimeExpire(DateUtil.format(payOrder.getExpiredTime(), DatePattern.NORM_DATETIME_FORMAT));  // 订单超时时间
            model.setProductCode("FAST_INSTANT_TRADE_PAY");
            model.setQrPayMode("4"); //订单码-跳转模式
            model.setQrcodeWidth(200L);
            req.setNotifyUrl(sysConfigService.getDBApplicationConfig().getPaySiteUrl() + "/api/pay/notify/alipay"); // 设置异步通知地址
            req.setBizModel(model);

            //统一放置 isv接口必传信息
            AlipayKit.putApiIsvInfo(mchAppConfigContext, req, model);

            formContent = configContextQueryService.getAlipayClientWrapper(mchAppConfigContext).getAlipayClient().pageExecute(req).getBody();

        }
        return formContent;
    }
}
