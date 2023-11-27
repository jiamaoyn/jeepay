package com.jeequan.alibill.channel.alipay;

import com.alipay.api.domain.AccountLogItemResult;
import com.alipay.api.domain.AlipayDataBillAccountlogQueryModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.request.AlipayDataBillAccountlogQueryRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayDataBillAccountlogQueryResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchApp;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.service.impl.MchAppService;
import com.jeequan.alibill.channel.IPayOrderQueryService;
import com.jeequan.alibill.model.MchAppConfigContext;
import com.jeequan.alibill.rqrs.msg.ChannelRetMsg;
import com.jeequan.alibill.service.ConfigContextQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
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
    @Autowired
    private MchAppService mchAppService;
    @Override
    public String getIfCode() {
        return CS.IF_CODE.ALIPAY;
    }

    @Override
    public ChannelRetMsg query(PayOrder payOrder, MchAppConfigContext mchAppConfigContext, Date startDate, Date endDate) {
        if (payOrder.getWayCode().equals("ALI_BILL")){
            AlipayDataBillAccountlogQueryRequest request = new AlipayDataBillAccountlogQueryRequest();
            AlipayDataBillAccountlogQueryModel model = new AlipayDataBillAccountlogQueryModel();
            // 获取当前时间
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            model.setStartTime(sdf.format(endDate));
            model.setEndTime(sdf.format(startDate));
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
                log.error("{},账号有问题！错误信息：{}", mchAppConfigContext.getAppId(), resp.getMsg()+resp.getSubMsg());
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
    public List<AccountLogItemResult> query(MchAppConfigContext mchAppConfigContext, Date startDate, Date endDate) {
        AlipayDataBillAccountlogQueryRequest request = new AlipayDataBillAccountlogQueryRequest();
        AlipayDataBillAccountlogQueryModel model = new AlipayDataBillAccountlogQueryModel();
        // 获取当前时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        model.setStartTime(sdf.format(endDate));
        model.setEndTime(sdf.format(startDate));
        request.setBizModel(model);
        //统一放置 isv接口必传信息
        AlipayKit.putApiIsvInfo(mchAppConfigContext, request, model);
        AlipayDataBillAccountlogQueryResponse resp = configContextQueryService.getAlipayClientWrapper(mchAppConfigContext).execute(request);
        if(resp.isSuccess()){
            return resp.getDetailList();
        } else {
            log.error("{},账号有问题！错误信息：{}", mchAppConfigContext.getAppId(), resp.getMsg()+resp.getSubMsg());
            return null;
        }
    }
}
