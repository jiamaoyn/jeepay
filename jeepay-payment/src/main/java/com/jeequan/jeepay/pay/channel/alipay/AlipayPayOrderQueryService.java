package com.jeequan.jeepay.pay.channel.alipay;

import cn.hutool.core.date.DateUtil;
import com.alipay.api.domain.*;
import com.alipay.api.request.AlipayDataBillAccountlogQueryRequest;
import com.alipay.api.request.AlipayDataBillTransferQueryRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayDataBillAccountlogQueryResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.pay.channel.IPayOrderQueryService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.service.ConfigContextQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/*
 * 支付宝 查单接口实现类
 */
@Service
@Slf4j
public class AlipayPayOrderQueryService implements IPayOrderQueryService {

    @Autowired
    private ConfigContextQueryService configContextQueryService;

    @Override
    public String getIfCode() {
        return CS.IF_CODE.ALIPAY;
    }

    @Override
    public ChannelRetMsg query(PayOrder payOrder, MchAppConfigContext mchAppConfigContext) {
        if (payOrder.getWayCode().equals("ALI_BILL")){
            AlipayDataBillAccountlogQueryRequest request = new AlipayDataBillAccountlogQueryRequest();
            AlipayDataBillAccountlogQueryModel model = new AlipayDataBillAccountlogQueryModel();
            // 获取当前时间
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime offsetDate = now.minusMinutes(120L);
            model.setStartTime(offsetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            model.setEndTime(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            request.setBizModel(model);
            AlipayDataBillAccountlogQueryResponse resp = configContextQueryService.getAlipayClientWrapper(mchAppConfigContext).execute(request);
            if(resp.isSuccess()){
                List<AccountLogItemResult> transferDetailResults = resp.getDetailList();
                if (transferDetailResults!=null){
                    for (AccountLogItemResult accountLogItemResult : transferDetailResults) {
                        if (accountLogItemResult.getTransMemo()!=null && accountLogItemResult.getTransMemo().equals(payOrder.getPayOrderId()) && Long.parseLong(AmountUtil.convertDollar2Cent(accountLogItemResult.getTransAmount())) == payOrder.getAmount()) {
                            log.info("alipay_order_no:{},balance:{},trans_amount:{},direction:{},trans_dt:{},trans_memo:{}" ,
                                    accountLogItemResult.getAlipayOrderNo(),accountLogItemResult.getBalance(),accountLogItemResult.getTransAmount(),accountLogItemResult.getDirection(),accountLogItemResult.getTransDt(),accountLogItemResult.getTransMemo());
                            return ChannelRetMsg.confirmSuccess(accountLogItemResult.getAlipayOrderNo());  //支付成功
                        }
                    }
                }
            } else {
                System.out.println(resp.getBody()+resp.getMsg()+resp.getSubCode());
            }
            return ChannelRetMsg.waiting(); //支付中
        }
        AlipayTradeQueryRequest req = new AlipayTradeQueryRequest();
        // 商户订单号，商户网站订单系统中唯一订单号，必填
        AlipayTradeQueryModel model = new AlipayTradeQueryModel();
        model.setOutTradeNo(payOrder.getPayOrderId());
        req.setBizModel(model);

        //通用字段
        AlipayKit.putApiIsvInfo(mchAppConfigContext, req, model);

        AlipayTradeQueryResponse resp = configContextQueryService.getAlipayClientWrapper(mchAppConfigContext).execute(req);
        String result = resp.getTradeStatus();

        if ("TRADE_SUCCESS".equals(result)) {
            return ChannelRetMsg.confirmSuccess(resp.getTradeNo());  //支付成功
        } else if ("WAIT_BUYER_PAY".equals(result)) {
            return ChannelRetMsg.waiting(); //支付中
        }
        return ChannelRetMsg.waiting(); //支付中
    }

    @Override
    public ChannelRetMsg queryTelegramBot(PayOrder payOrder, MchAppConfigContext mchAppConfigContext){
        if (payOrder.getWayCode().equals("ALI_BILL")){
            AlipayDataBillAccountlogQueryRequest request = new AlipayDataBillAccountlogQueryRequest();
            AlipayDataBillAccountlogQueryModel model = new AlipayDataBillAccountlogQueryModel();
            // 获取当前时间
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime threeMinutesAgo = now.minusDays(3);
            model.setStartTime(threeMinutesAgo.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            model.setEndTime(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            model.setAlipayOrderNo(payOrder.getChannelOrderNo());
            request.setBizModel(model);
            AlipayDataBillAccountlogQueryResponse resp = configContextQueryService.getAlipayClientWrapper(mchAppConfigContext).execute(request);
            if(resp.isSuccess()){
                List<AccountLogItemResult> transferDetailResults = resp.getDetailList();
                if (transferDetailResults!=null){
                    for (AccountLogItemResult accountLogItemResult : transferDetailResults) {
                        System.out.println(Long.parseLong(AmountUtil.convertDollar2Cent(accountLogItemResult.getTransAmount())) == payOrder.getAmount());
                        System.out.println(payOrder.getAmount());
                        System.out.println(Long.parseLong(AmountUtil.convertDollar2Cent(accountLogItemResult.getTransAmount())));
                        if (Long.parseLong(AmountUtil.convertDollar2Cent(accountLogItemResult.getTransAmount())) == payOrder.getAmount()) {
                            System.out.println("accountLogItemResult-------------start");
                            System.out.println("alipay_order_no:" + accountLogItemResult.getAlipayOrderNo());
                            System.out.println("balance:" + accountLogItemResult.getBalance());
                            System.out.println("trans_amount:" + accountLogItemResult.getTransAmount());
                            System.out.println("direction:" + accountLogItemResult.getDirection());
                            System.out.println("trans_dt:" + accountLogItemResult.getTransDt());
                            System.out.println("trans_memo:" + accountLogItemResult.getTransMemo());
                            System.out.println("accountLogItemResult-------------end");
                            return ChannelRetMsg.confirmSuccess(accountLogItemResult.getAlipayOrderNo());  //支付成功
                        } else {
                            return ChannelRetMsg.unknown("未查到匹配的订单，请检查金额或单号");  //支付成功
                        }
                    }
                }
            } else {
                System.out.println(resp.getBody()+resp.getMsg()+resp.getSubCode());
            }
            return ChannelRetMsg.waiting(); //支付中
        }
        AlipayTradeQueryRequest req = new AlipayTradeQueryRequest();
        // 商户订单号，商户网站订单系统中唯一订单号，必填
        AlipayTradeQueryModel model = new AlipayTradeQueryModel();
        model.setOutTradeNo(payOrder.getPayOrderId());
        req.setBizModel(model);

        //通用字段
        AlipayKit.putApiIsvInfo(mchAppConfigContext, req, model);

        AlipayTradeQueryResponse resp = configContextQueryService.getAlipayClientWrapper(mchAppConfigContext).execute(req);
        String result = resp.getTradeStatus();

        if ("TRADE_SUCCESS".equals(result)) {
            return ChannelRetMsg.confirmSuccess(resp.getTradeNo());  //支付成功
        } else if ("WAIT_BUYER_PAY".equals(result)) {
            return ChannelRetMsg.waiting(); //支付中
        }
        return ChannelRetMsg.waiting(); //支付中
    }


}
