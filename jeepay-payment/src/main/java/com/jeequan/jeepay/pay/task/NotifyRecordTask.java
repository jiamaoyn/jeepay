package com.jeequan.jeepay.pay.task;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jeequan.jeepay.core.entity.MchNotifyRecord;
import com.jeequan.jeepay.pay.service.PayMchNotifyService;
import com.jeequan.jeepay.service.impl.MchNotifyRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
@Slf4j
@Component
public class NotifyRecordTask {
    private static final int QUERY_PAGE_SIZE = 100; //每次查询数量
    @Autowired
    private MchNotifyRecordService mchNotifyRecordService;
    @Autowired
    private PayMchNotifyService payMchNotifyService;
    @Scheduled(cron = "0 0/1 * * * ?") // 每分钟执行一次
    public void start() {

        //当前时间 减去10分钟。
        Date offsetDate = DateUtil.offsetMinute(new Date(), -5);

        //查询条件： 支付中的订单 & （ 订单创建时间 + 10分钟 >= 当前时间 ）
        LambdaQueryWrapper<MchNotifyRecord> lambdaQueryWrapper = MchNotifyRecord.gw().eq(MchNotifyRecord::getState, MchNotifyRecord.STATE_ING).eq(MchNotifyRecord::getOrderType, MchNotifyRecord.TYPE_PAY_ORDER).le(MchNotifyRecord::getCreatedAt, offsetDate);
        int currentPageIndex = 1; //当前页码
        while (true) {
            try {
                IPage<MchNotifyRecord> mchNotifyRecordIPage = mchNotifyRecordService.page(new Page(currentPageIndex, QUERY_PAGE_SIZE), lambdaQueryWrapper);

                if (mchNotifyRecordIPage == null || mchNotifyRecordIPage.getRecords().isEmpty()) { //本次查询无结果, 不再继续查询;
                    break;
                }
                for (MchNotifyRecord mchNotifyRecord : mchNotifyRecordIPage.getRecords()) {
                    payMchNotifyService.notifyRecord(mchNotifyRecord);
                }
                //已经到达页码最大量，无需再次查询
                if (mchNotifyRecordIPage.getPages() <= currentPageIndex) {
                    break;
                }
                currentPageIndex++;
            } catch (Exception e) { //出现异常，直接退出，避免死循环。
                log.error("error", e);
                break;
            }

        }
    }
}
