package com.jeequan.jeepay.pay.channel;

import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

/*
 * 渠道侧的支付订单通知解析实现 【分为同步跳转（doReturn）和异步回调(doNotify) 】
 * @date 2021/5/8 15:14
 */
public interface IChannelNoticeService {

    /**
     * 获取到接口code
     **/
    String getIfCode();

    /**
     * 解析参数： 订单号 和 请求参数
     * 异常需要自行捕捉，并返回null , 表示已响应数据。
     **/
    MutablePair<String, Object> parseParams(HttpServletRequest request, String urlOrderId, NoticeTypeEnum noticeTypeEnum);

    /**
     * 返回需要更新的订单状态 和响应数据
     **/
    ChannelRetMsg doNotice(HttpServletRequest request,
                           Object params, PayOrder payOrder, MchAppConfigContext mchAppConfigContext, NoticeTypeEnum noticeTypeEnum);

    /**
     * 数据库订单 状态更新异常 (仅异步通知使用)
     **/
    ResponseEntity doNotifyOrderStateUpdateFail(HttpServletRequest request);

    /**
     * 数据库订单数据不存在  (仅异步通知使用)
     **/
    ResponseEntity doNotifyOrderNotExists(HttpServletRequest request);

    /**
     * 通知类型
     **/
    enum NoticeTypeEnum {
        DO_RETURN, //同步跳转
        DO_NOTIFY //异步回调
    }

}
