package com.jeequan.jeepay.pay.ctrl.payorder;

import cn.hutool.core.util.URLUtil;
import com.jeequan.jeepay.core.entity.MchApp;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.model.params.alipay.AlipayNormalMchParams;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.pay.ctrl.ApiController;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.payorder.QueryPayOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.QueryPayOrderRS;
import com.jeequan.jeepay.pay.service.ConfigContextQueryService;
import com.jeequan.jeepay.service.impl.PayOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

/**
 * 商户查单controller
 */
@Slf4j
@RestController
public class QueryOrderController extends ApiController {

    @Autowired
    private PayOrderService payOrderService;
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
        AlipayNormalMchParams normalMchParams = (AlipayNormalMchParams) configContextQueryService.queryNormalMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), "alipay");
        if (normalMchParams == null) {
            throw new BizException("商户支付宝接口没有配置！");
        }
        if (payOrder.getState() == PayOrder.STATE_INIT){
            boolean isSuccess = payOrderService.updateInit2Ing(payOrder.getPayOrderId(), payOrder);
            if (!isSuccess) {
                throw new BizException("更新订单异常!");
            }
        }
        response.sendRedirect("https://ds.alipay.com/?from=pc&appId=20000116&actionType=toAccount&goBack=NO&amount="+ AmountUtil.convertCent2Dollar(payOrder.getAmount().toString())+"&userId="+normalMchParams.getPid()+"&memo="+payOrderId);
    }

}
