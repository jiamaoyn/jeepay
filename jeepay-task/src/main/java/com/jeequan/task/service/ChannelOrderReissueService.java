package com.jeequan.task.service;

import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.utils.SpringBeansUtil;
import com.jeequan.task.channel.IPayOrderQueryService;
import com.jeequan.task.model.MchAppConfigContext;
import com.jeequan.task.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.service.impl.PayOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

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
    public void processPayOrderBill(PayOrder payOrder, Date startDate, Date endDate) {
        try {
            String payOrderId = payOrder.getPayOrderId();
            payOrder = payOrderService.queryMchOrder(payOrderId);
            if (payOrder.getState() == PayOrder.STATE_SUCCESS){
                return;
            }
            //查询支付接口是否存在
            IPayOrderQueryService queryService = SpringBeansUtil.getBean(payOrder.getIfCode() + "PayOrderQueryService", IPayOrderQueryService.class);
            // 支付通道接口实现不存在
            if (queryService == null) {
                log.error("{} interface not exists!", payOrder.getIfCode());
                return;
            }
            //查询出商户应用的配置信息
            MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(payOrder.getMchNo(), payOrder.getAppId());
            ChannelRetMsg channelRetMsg = queryService.query(payOrder, mchAppConfigContext, startDate, endDate);
            if (channelRetMsg == null) {
                log.error("channelRetMsg is null");
                return;
            }
            log.info("补单[{}]查询结果为：{}", payOrderId, channelRetMsg.getChannelState());
            // 查询成功
            if (channelRetMsg.getChannelState() == ChannelRetMsg.ChannelState.CONFIRM_SUCCESS) {
                if (payOrderService.updateIng2Success(payOrderId, channelRetMsg.getChannelOrderId(), channelRetMsg.getChannelUserId())) {
                    //订单支付成功，其他业务逻辑
                    payOrderProcessService.confirmSuccessPolling(payOrder);
                }
            }
        } catch (Exception e) {  //继续下一次迭代查询
            log.error("error payOrderId = {}", payOrder.getPayOrderId(), e);
        }
    }


}
