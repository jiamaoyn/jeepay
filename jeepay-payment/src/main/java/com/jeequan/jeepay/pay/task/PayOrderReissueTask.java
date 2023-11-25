package com.jeequan.jeepay.pay.task;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.pay.service.ChannelOrderReissueService;
import com.jeequan.jeepay.service.impl.PayOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/*
 * 补单定时任务
 */
@Slf4j
@Component
public class PayOrderReissueTask {

    private static final int QUERY_PAGE_SIZE = 50; //每次查询数量

    @Autowired
    private PayOrderService payOrderService;
    @Autowired
    private ChannelOrderReissueService channelOrderReissueService;

    @Scheduled(cron = "0 0/1 * * * ?") // 每分钟执行一次
    public void start() {

        //当前时间 减去10分钟。
        Date offsetDate = DateUtil.offsetMinute(new Date(), -10);

        //查询条件： 支付中的订单 & （ 订单创建时间 + 10分钟 >= 当前时间 ）
        LambdaQueryWrapper<PayOrder> lambdaQueryWrapper = PayOrder.gw().eq(PayOrder::getState, PayOrder.STATE_ING).notIn(PayOrder::getWayCode, "ALI_BILL").le(PayOrder::getCreatedAt, offsetDate);
        int currentPageIndex = 1; //当前页码
        while (true) {
            try {
                IPage<PayOrder> payOrderIPage = payOrderService.page(new Page(currentPageIndex, QUERY_PAGE_SIZE), lambdaQueryWrapper);

                if (payOrderIPage == null || payOrderIPage.getRecords().isEmpty()) { //本次查询无结果, 不再继续查询;
                    break;
                }
                for (PayOrder payOrder : payOrderIPage.getRecords()) {
                    channelOrderReissueService.processPayOrder(payOrder);
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

    @Scheduled(cron = "*/2 * * * * ?") // 每2秒钟执行一次
    public void start_bill() {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        Date onsetDate = DateUtil.offsetMinute(new Date(), -1);
        LambdaQueryWrapper<PayOrder> lambdaQueryWrapper = PayOrder.gw().eq(PayOrder::getState, PayOrder.STATE_ING).in(PayOrder::getWayCode, "ALI_BILL").ge(PayOrder::getCreatedAt, onsetDate);
        int currentPageIndex = 1; //当前页码
        while (true) {
            try {
                IPage<PayOrder> payOrderIPage = payOrderService.page(new Page(currentPageIndex, QUERY_PAGE_SIZE), lambdaQueryWrapper);
                if (payOrderIPage == null || payOrderIPage.getRecords().isEmpty()) { //本次查询无结果, 不再继续查询;
                    log.info("本次查询无结果, 不再继续查询");
                    break;
                }
                for (PayOrder payOrder : payOrderIPage.getRecords()) {
                    executor.submit(() -> channelOrderReissueService.processPayOrderBill(payOrder));
                }
                //已经到达页码最大量，无需再次查询
                if (payOrderIPage.getPages() <= currentPageIndex) {
                    break;
                }
                currentPageIndex++;
                executor.shutdown();
                try {
                    if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                        executor.shutdownNow(); // 超时后强制关闭
                    }
                } catch (InterruptedException e) {
                    executor.shutdownNow(); // 如果等待被中断也强制关闭
                    Thread.currentThread().interrupt(); // 重新设置中断状态
                    log.error("Executor service interrupted", e);
                }
            } catch (Exception e) { //出现异常，直接退出，避免死循环。
                log.error("error", e);
                break;
            }

        }
    }


}
