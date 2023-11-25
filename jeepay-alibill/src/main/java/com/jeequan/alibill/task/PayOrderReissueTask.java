package com.jeequan.alibill.task;

import cn.hutool.core.date.DateUtil;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchApp;
import com.jeequan.jeepay.core.entity.MchPayPassage;
import com.jeequan.jeepay.service.impl.MchAppService;
import com.jeequan.jeepay.service.impl.MchPayPassageService;
import com.jeequan.alibill.service.ChannelOrderReissueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/*
 * 补单定时任务
 */
@Slf4j
@Component
public class PayOrderReissueTask {

    private static final int QUERY_PAGE_SIZE = 50; //每次查询数量

    @Autowired
    private MchAppService mchAppService;
    @Autowired
    private MchPayPassageService mchPayPassageService;
    @Autowired
    private ChannelOrderReissueService channelOrderReissueService;
    @Scheduled(cron = "*/5 * * * * ?") // 每2秒钟执行一次
    public void start_bill1() {
        Date startDate = DateUtil.offsetMinute(new Date(), -0);
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
    @Scheduled(cron = "* */5 * * * ?") // 每2秒钟执行一次
    public void start_bill5() {
        Date startDate = DateUtil.offsetMinute(new Date(), -30);
        Date endDate = DateUtil.offsetMinute(new Date(), -100);
        startBillDateExecutorService(startDate, endDate);
    }
    public void startBillDateExecutorService(Date startDate, Date endDate) {
        List<MchApp> mchAppList = new ArrayList<>();
        mchAppService.list(MchApp.gw().eq(MchApp::getState, CS.PUB_USABLE)).forEach(mchApp -> {
            MchPayPassage payInterfaceConfig = mchPayPassageService.getOne(MchPayPassage.gw()
                    .select(MchPayPassage::getIfCode, MchPayPassage::getAppId)
                    .eq(MchPayPassage::getState, CS.PUB_USABLE)
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
        mchAppList.forEach(mchapp -> {
            log.info("当前查询appId：{}---MchNo:{}---appName:{}",mchapp.getAppId(),mchapp.getMchNo(),mchapp.getAppName());
            channelOrderReissueService.processPayOrderBill(mchapp, startDate, endDate);
        });
    }
}
