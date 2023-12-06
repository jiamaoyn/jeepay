package com.jeetask.telegram.task;

import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchApp;
import com.jeequan.jeepay.core.entity.MchPayPassage;
import com.jeequan.jeepay.service.impl.MchAppService;
import com.jeequan.jeepay.service.impl.MchPayPassageService;
import com.jeetask.telegram.service.TelegramService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
    private TelegramService telegramService;

    @Scheduled(cron = "*/1 * * * * ?") // 每2秒钟执行一次
    public void check_app() {
        checkAppService();
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
            telegramService.checkAppService(mchApp);
        }
    }
}
