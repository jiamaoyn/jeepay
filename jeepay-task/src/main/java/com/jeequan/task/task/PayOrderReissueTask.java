package com.jeequan.task.task;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.task.service.ChannelOrderReissueService;
import com.jeequan.jeepay.service.impl.PayOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

/*
 * 补单定时任务
 */
@Slf4j
@Component
public class PayOrderReissueTask {

    private static final int QUERY_PAGE_SIZE = 100; //每次查询数量

    @Autowired
    private PayOrderService payOrderService;
    @Autowired
    private ChannelOrderReissueService channelOrderReissueService;

    @Scheduled(cron = "*/3 * * * * ?") // 每2秒钟执行一次
    public void start_bill2() {
        Date startDate = DateUtil.offsetMinute(new Date(), -1);
        Date endDate = DateUtil.offsetMinute(new Date(), -2);
        startBillDate(startDate, endDate);
    }
    @Scheduled(cron = "*/4 * * * * ?") // 每2秒钟执行一次
    public void start_bill4() {
        Date startDate = DateUtil.offsetMinute(new Date(), -2);
        Date endDate = DateUtil.offsetMinute(new Date(), -4);
        startBillDate(startDate, endDate);
    }
    @Scheduled(cron = "*/5 * * * * ?") // 每2秒钟执行一次
    public void start_bill6() {
        Date startDate = DateUtil.offsetMinute(new Date(), -4);
        Date endDate = DateUtil.offsetMinute(new Date(), -6);
        startBillDate(startDate, endDate);
    }
    @Scheduled(cron = "*/6 * * * * ?") // 每2秒钟执行一次
    public void start_bill10() {
        Date startDate = DateUtil.offsetMinute(new Date(), -6);
        Date endDate = DateUtil.offsetMinute(new Date(), -10);
        startBillDate(startDate, endDate);
    }
    @Scheduled(cron = "*/8 * * * * ?") // 每2秒钟执行一次
    public void start_bill20() {
        Date startDate = DateUtil.offsetMinute(new Date(), -10);
        Date endDate = DateUtil.offsetMinute(new Date(), -20);
        startBillDate(startDate, endDate);
    }
    @Scheduled(cron = "*/10 * * * * ?") // 每2秒钟执行一次
    public void start_bill40() {
        Date startDate = DateUtil.offsetMinute(new Date(), -20);
        Date endDate = DateUtil.offsetMinute(new Date(), -40);
        startBillDate(startDate, endDate);
    }
    @Scheduled(cron = "*/20 * * * * ?") // 每2秒钟执行一次
    public void start_bill60() {
        Date startDate = DateUtil.offsetMinute(new Date(), -40);
        Date endDate = DateUtil.offsetMinute(new Date(), -60);
        startBillDate(startDate, endDate);
    }
    @Scheduled(cron = "*/30 * * * * ?") // 每2秒钟执行一次
    public void start_bill120() {
        Date startDate = DateUtil.offsetMinute(new Date(), -60);
        Date endDate = DateUtil.offsetMinute(new Date(), -120);
        startBillDate(startDate, endDate);
    }
    @Scheduled(cron = "* */1 * * * ?") // 每2秒钟执行一次
    public void start_bill240() {
        Date startDate = DateUtil.offsetMinute(new Date(), -120);
        Date endDate = DateUtil.offsetMinute(new Date(), -240);
        startBillDate(startDate, endDate);
    }
    public void startBillDate(Date startDate, Date endDate) {
        LambdaQueryWrapper<PayOrder> lambdaQueryWrapper = PayOrder.gw().eq(PayOrder::getState, PayOrder.STATE_ING).in(PayOrder::getWayCode, "ALI_BILL").le(PayOrder::getCreatedAt, startDate).ge(PayOrder::getCreatedAt, endDate);
        int currentPageIndex = 1; //当前页码
        while (true) {
            try {
                IPage<PayOrder> payOrderIPage = payOrderService.page(new Page(currentPageIndex, QUERY_PAGE_SIZE), lambdaQueryWrapper);
                if (payOrderIPage == null || payOrderIPage.getRecords().isEmpty()) { //本次查询无结果, 不再继续查询;
                    log.info("本次查询无结果, 不再继续查询");
                    break;
                }
                for (PayOrder payOrder : payOrderIPage.getRecords()) {
                    log.info("本次查询payOrder"+payOrder);
                    channelOrderReissueService.processPayOrderBill(payOrder, startDate, endDate);
                }
                //已经到达页码最大量，无需再次查询
                if (payOrderIPage.getPages() <= currentPageIndex) {
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
