package com.jeequan.alibill.task;

import cn.hutool.core.date.DateUtil;
import com.jeequan.alibill.service.ChannelOrderReissueService;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchApp;
import com.jeequan.jeepay.core.entity.MchPayPassage;
import com.jeequan.jeepay.service.impl.MchAppService;
import com.jeequan.jeepay.service.impl.MchPayPassageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/*
 * 补单定时任务
 */
@Slf4j
@Component
public class PayOrderReissueTask {

    @Autowired
    private MchAppService mchAppService;
    @Autowired
    private MchPayPassageService mchPayPassageService;
    @Autowired
    private ChannelOrderReissueService channelOrderReissueService;

    @Scheduled(cron = "*/1 * * * * ?") // 每2秒钟执行一次
    public void check_app() {
        checkAppService();
    }

    @Scheduled(cron = "*/1 * * * * ?") // 每2秒钟执行一次
    public void start_bill() {
        Date startDate = DateUtil.offsetMinute(new Date(), -0);
        Date endDate = DateUtil.offsetMinute(new Date(), -1);
        startBillDateExecutorService(startDate, endDate);
    }

    @Scheduled(cron = "*/3 * * * * ?") // 每2秒钟执行一次
    public void start_bill1() {
        Date startDate = DateUtil.offsetMinute(new Date(), -1);
        Date endDate = DateUtil.offsetMinute(new Date(), -3);
        startBillDateExecutorService(startDate, endDate);
    }

    @Scheduled(cron = "*/5 * * * * ?") // 每2秒钟执行一次
    public void start_bill0() {
        Date startDate = DateUtil.offsetMinute(new Date(), -3);
        Date endDate = DateUtil.offsetMinute(new Date(), -5);
        startBillDateExecutorService(startDate, endDate);
    }

    @Scheduled(cron = "*/30 * * * * ?") // 每2秒钟执行一次
    public void start_bill2() {
        Date startDate = DateUtil.offsetMinute(new Date(), -5);
        Date endDate = DateUtil.offsetMinute(new Date(), -10);
        startBillDateExecutorService(startDate, endDate);
    }

    @Scheduled(cron = "* */1 * * * ?") // 每2秒钟执行一次
    public void start_bill3() {
        Date startDate = DateUtil.offsetMinute(new Date(), -10);
        Date endDate = DateUtil.offsetMinute(new Date(), -30);
        startBillDateExecutorService(startDate, endDate);
    }

    @Scheduled(cron = "* */10 * * * ?") // 每2秒钟执行一次
    public void start_bill5() {
        Date startDate = DateUtil.offsetMinute(new Date(), -30);
        Date endDate = DateUtil.offsetMinute(new Date(), -120);
        startBillDateExecutorService(startDate, endDate);
    }

    public void startBillDateExecutorService(Date startDate, Date endDate) {
        List<MchApp> mchAppList = new ArrayList<>();
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        mchAppService.list(
                MchApp.gw().eq(MchApp::getState, CS.YES).or(wrapper -> wrapper
                        .eq(MchApp::getState, CS.NO)
                        .ge(MchApp::getUpdatedAt, fiveMinutesAgo)
                )).forEach(mchApp -> {
            MchPayPassage payInterfaceConfig = mchPayPassageService.getOne(MchPayPassage.gw()
                    .select(MchPayPassage::getIfCode, MchPayPassage::getAppId)
                    .eq(MchPayPassage::getState, CS.YES)
                    .eq(MchPayPassage::getAppId, mchApp.getAppId())
                    .eq(MchPayPassage::getWayCode, "ALI_BILL")
            );
            if (payInterfaceConfig != null) {
                mchAppList.add(mchApp);
            }
        });
        if (mchAppList.isEmpty()) {
            return;
        }
        ExecutorService executor = Executors.newFixedThreadPool(16); // 根据服务器性能调整线程数
        try {
            for (MchApp mchApp : mchAppList) {
                executor.submit(() -> {
                    log.info("当前查询appId：{}---MchNo:{}---appName:{}", mchApp.getAppId(), mchApp.getMchNo(), mchApp.getAppName());
                    channelOrderReissueService.processPayOrderBill(mchApp, startDate, endDate);
                });
            }
        } finally {
            executor.shutdown();
            try {
                executor.awaitTermination(1, TimeUnit.MINUTES); // 根据需要设置超时时间
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void checkAppService() {
        List<MchApp> mchAppList = new ArrayList<>();
        mchAppService.list(MchApp.gw().eq(MchApp::getState, CS.YES)).forEach(mchApp -> {
            MchPayPassage payInterfaceConfig = mchPayPassageService.getOne(MchPayPassage.gw()
                    .select(MchPayPassage::getIfCode, MchPayPassage::getAppId)
                    .eq(MchPayPassage::getState, CS.YES)
                    .eq(MchPayPassage::getAppId, mchApp.getAppId())
                    .eq(MchPayPassage::getWayCode, "ALI_BILL")
            );
            if (payInterfaceConfig != null) {
                mchAppList.add(mchApp);
            }
        });
        if (mchAppList.isEmpty()) {
            return;
        }
        for (MchApp mchApp : mchAppList) {
            channelOrderReissueService.checkAppService(mchApp);
        }
    }
}
