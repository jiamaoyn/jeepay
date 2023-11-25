package com.jeequan.alibill.service;

import com.alipay.api.domain.AccountLogItemResult;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchApp;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.core.utils.SpringBeansUtil;
import com.jeequan.jeepay.service.impl.MchAppService;
import com.jeequan.jeepay.service.impl.PayOrderService;
import com.jeequan.alibill.channel.IPayOrderQueryService;
import com.jeequan.alibill.model.MchAppConfigContext;
import com.jeequan.alibill.rqrs.msg.ChannelRetMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/*
 * 查询上游订单， &  补单服务实现类
 * @date 2021/6/8 17:40
 */

@Service
@Slf4j
public class ChannelOrderReissueService {

    @Autowired
    private ConfigContextQueryService configContextQueryService;
    @Autowired
    private PayOrderService payOrderService;
    @Autowired
    private PayOrderProcessService payOrderProcessService;
    public void processPayOrderBill(MchApp mchApp, Date startDate, Date endDate) {
        try {
            //查询支付接口是否存在
            IPayOrderQueryService queryService = SpringBeansUtil.getBean("alipayPayOrderQueryService", IPayOrderQueryService.class);
            // 支付通道接口实现不存在
            if (queryService == null) {
                log.error("{} interface not exists error!", "alipay");
                return;
            }
            //查询出商户应用的配置信息
            MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(mchApp.getMchNo(), mchApp.getAppId());
            List<AccountLogItemResult> accountLogItemResultList = queryService.query(mchAppConfigContext, startDate, endDate);
            if (accountLogItemResultList == null) {
                log.error("appid={},未查询到支付宝订单，结束时间：{},开始时间:{}", mchAppConfigContext.getAppId(), startDate, endDate);
                return;
            }
            accountLogItemResultList.forEach(accountLogItemResult -> {
//                log.info("alipay_order_no:{},balance:{},trans_amount:{},direction:{},trans_dt:{},trans_memo:{}" ,
//                        accountLogItemResult.getAlipayOrderNo(),accountLogItemResult.getBalance(),accountLogItemResult.getTransAmount(),accountLogItemResult.getDirection(),accountLogItemResult.getTransDt(),accountLogItemResult.getTransMemo());
                if (accountLogItemResult.getTransMemo()!=null) {
                    PayOrder payOrder = payOrderService.queryPayOrderIdNoStateIng(accountLogItemResult.getTransMemo());
                    if (payOrder == null){
                        return;
                    }
                    System.out.println("查找订单号："+accountLogItemResult.getTransMemo()+"查找订单号结果"+payOrder);
                    if (Long.parseLong(AmountUtil.convertDollar2Cent(accountLogItemResult.getTransAmount())) == payOrder.getAmount()){
                        if (payOrderService.updateIng2Success(payOrder.getPayOrderId(), accountLogItemResult.getAlipayOrderNo(), null)) {
                            //订单支付成功，其他业务逻辑
                            payOrderProcessService.confirmSuccessPolling(payOrder);
                            log.info("订单支付成功[{}],alipay_order_no:{},balance:{},trans_amount:{},direction:{},trans_dt:{},trans_memo:{}" ,
                                    payOrder.getPayOrderId(),accountLogItemResult.getAlipayOrderNo(),accountLogItemResult.getBalance(),accountLogItemResult.getTransAmount(),accountLogItemResult.getDirection(),accountLogItemResult.getTransDt(),accountLogItemResult.getTransMemo());
                        }
                    }
                }

            });
        } catch (Exception e) {  //继续下一次迭代查询
            log.error("error 支付宝商家订单回调", e);
        }
    }


}
