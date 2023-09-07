package com.jeequan.jeepay.pay.service;

import com.jeequan.jeepay.core.entity.RefundOrder;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.service.impl.RefundOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/***
 * 退款处理通用逻辑
 *
 * @author jmdhappy
 * @site https://www.jeequan.com
 * @date 2021/9/25 23:50
 */
@Service
@Slf4j
public class RefundOrderProcessService {

    @Autowired
    private RefundOrderService refundOrderService;
    @Autowired
    private PayMchNotifyService payMchNotifyService;

    /**
     * 根据通道返回的状态，处理退款订单业务
     **/
    public boolean handleRefundOrder4Channel(ChannelRetMsg channelRetMsg, RefundOrder refundOrder) {
        boolean updateOrderSuccess = true; //默认更新成功
        String refundOrderId = refundOrder.getRefundOrderId();
        // 明确退款成功
        if (channelRetMsg.getChannelState() == ChannelRetMsg.ChannelState.CONFIRM_SUCCESS) {
            updateOrderSuccess = refundOrderService.updateIng2Success(refundOrderId, channelRetMsg.getChannelOrderId());
            if (updateOrderSuccess) {
                // 通知商户系统
                if (StringUtils.isNotEmpty(refundOrder.getNotifyUrl())) {
                    payMchNotifyService.refundOrderNotify(refundOrderService.getById(refundOrderId));
                }
            }
            //确认失败
        } else if (channelRetMsg.getChannelState() == ChannelRetMsg.ChannelState.CONFIRM_FAIL) {
            // 更新为失败状态
            updateOrderSuccess = refundOrderService.updateIng2Fail(refundOrderId, channelRetMsg.getChannelOrderId(), channelRetMsg.getChannelErrCode(), channelRetMsg.getChannelErrMsg());
            // 通知商户系统
            if (StringUtils.isNotEmpty(refundOrder.getNotifyUrl())) {
                payMchNotifyService.refundOrderNotify(refundOrderService.getById(refundOrderId));
            }
        }
        return updateOrderSuccess;
    }

}
