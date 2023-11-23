package com.jeequan.jeepay.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jeequan.jeepay.core.entity.MchNotifyRecord;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.service.mapper.MchNotifyRecordMapper;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * <p>
 * 商户通知表 服务实现类
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2021-04-27
 */
@Service
public class MchNotifyRecordService extends ServiceImpl<MchNotifyRecordMapper, MchNotifyRecord> {

    /**
     * 根据订单号和类型查询
     */
    public MchNotifyRecord findByOrderAndType(String orderId, Byte orderType) {
        return getOne(
                MchNotifyRecord.gw().eq(MchNotifyRecord::getOrderId, orderId).eq(MchNotifyRecord::getOrderType, orderType)
        );
    }

    /**
     * 查询支付订单
     */
    public MchNotifyRecord findByPayOrder(String orderId) {
        return findByOrderAndType(orderId, MchNotifyRecord.TYPE_PAY_ORDER);
    }

    /**
     * 查询退款订单订单
     */
    public MchNotifyRecord findByRefundOrder(String orderId) {
        return findByOrderAndType(orderId, MchNotifyRecord.TYPE_REFUND_ORDER);
    }

    /**
     * 查询退款订单订单
     */
    public MchNotifyRecord findByTransferOrder(String transferId) {
        return findByOrderAndType(transferId, MchNotifyRecord.TYPE_TRANSFER_ORDER);
    }

    public Integer updateNotifyResult(Long notifyId, Byte state, String resResult) {
        return baseMapper.updateNotifyResult(notifyId, state, resResult);
    }

    /**
     * 更新通知信息  【x】 --》 【0】
     **/
    public boolean updateNotifyRecord(MchNotifyRecord mchNotifyRecord) {

        MchNotifyRecord updateRecord = new MchNotifyRecord();
        updateRecord.setNotifyCount(0);

        return update(updateRecord, new LambdaUpdateWrapper<MchNotifyRecord>()
                .eq(MchNotifyRecord::getNotifyId, mchNotifyRecord.getNotifyId()).eq(MchNotifyRecord::getState, MchNotifyRecord.STATE_ING));
    }


}
