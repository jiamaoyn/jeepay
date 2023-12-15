package com.jeequan.alibill.service;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.components.mq.model.PayOrderMchNotifyMQ;
import com.jeequan.jeepay.components.mq.vender.IMQSender;
import com.jeequan.jeepay.core.entity.MchNotifyRecord;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.utils.JeepayKit;
import com.jeequan.jeepay.core.utils.StringKit;
import com.jeequan.jeepay.service.impl.MchNotifyRecordService;
import com.jeequan.alibill.rqrs.payorder.QueryPayOrderRS;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 商户通知 service
 */
@Slf4j
@Service
public class PayMchNotifyService {

    @Autowired
    private MchNotifyRecordService mchNotifyRecordService;
    @Autowired
    private ConfigContextQueryService configContextQueryService;
    @Autowired
    private IMQSender mqSender;
    /**
     * 商户通知信息， 只有订单是终态，才会发送通知， 如明确成功和明确失败
     **/
    public void payOrderNotifyPolling(PayOrder dbPayOrder) {

        try {
            // 通知地址为空
            if (StringUtils.isEmpty(dbPayOrder.getNotifyUrl())) {
                return;
            }

            //获取到通知对象
            MchNotifyRecord mchNotifyRecord = mchNotifyRecordService.findByPayOrder(dbPayOrder.getPayOrderId());

            if (mchNotifyRecord != null) {
                log.info("当前已存在通知消息， 不再发送。");
                return;
            }
            String appSecret;
            if (dbPayOrder.getBusNo() == null) {
                //商户app私钥
                appSecret = configContextQueryService.queryMchInfo(dbPayOrder.getMchNo()).getSecret();
            } else {
                appSecret = configContextQueryService.queryMchInfo(dbPayOrder.getBusNo()).getSecret();
            }
            // 封装通知url
            String notifyUrl = createNotifyUrl(dbPayOrder, appSecret);
            mchNotifyRecord = new MchNotifyRecord();
            mchNotifyRecord.setOrderId(dbPayOrder.getPayOrderId());
            mchNotifyRecord.setOrderType(MchNotifyRecord.TYPE_PAY_ORDER);
            mchNotifyRecord.setMchNo(dbPayOrder.getMchNo());
            mchNotifyRecord.setMchOrderNo(dbPayOrder.getMchOrderNo()); //商户订单号
            mchNotifyRecord.setIsvNo(dbPayOrder.getIsvNo());
            mchNotifyRecord.setAppId(dbPayOrder.getAppId());
            mchNotifyRecord.setNotifyUrl(notifyUrl);
            mchNotifyRecord.setResResult("");
            mchNotifyRecord.setNotifyCount(0);
            mchNotifyRecord.setState(MchNotifyRecord.STATE_ING); // 通知中

            try {
                mchNotifyRecordService.save(mchNotifyRecord);
            } catch (Exception e) {
                log.info("数据库已存在[{}]消息，本次不再推送。", mchNotifyRecord.getOrderId());
                return;
            }

            //推送到MQ
            Long notifyId = mchNotifyRecord.getNotifyId();
            mqSender.send(PayOrderMchNotifyMQ.build(notifyId));

        } catch (Exception e) {
            log.error("推送失败！", e);
        }
    }

    /**
     * 创建响应URL
     */
    public String createNotifyUrl(PayOrder payOrder, String appSecret) {

        QueryPayOrderRS queryPayOrderRS = QueryPayOrderRS.buildByPayOrder(payOrder);
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(queryPayOrderRS);
        jsonObject.put("reqTime", System.currentTimeMillis()); //添加请求时间
        // 报文签名
        jsonObject.put("sign", JeepayKit.getSign(jsonObject, appSecret));

        // 生成通知
        return StringKit.appendUrlQuery(payOrder.getNotifyUrl(), jsonObject);
    }
}
