package com.jeetask.telegram.service;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jeetask.telegram.telegram.MyCustomBot;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.ctrls.AbstractCtrl;
import com.jeequan.jeepay.core.entity.MchApp;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.TelegramChat;
import com.jeequan.jeepay.service.impl.MchAppService;
import com.jeequan.jeepay.service.impl.PayOrderService;
import com.jeequan.jeepay.service.impl.SysConfigService;
import com.jeequan.jeepay.service.impl.TelegramChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

@Service
@Slf4j
public class TelegramService extends AbstractCtrl {

    @Autowired
    private MyCustomBot myCustomBot;
    @Autowired
    private TelegramChatService telegramChatService;
    @Autowired
    private PayOrderService payOrderService;
    @Autowired
    private MchAppService mchAppService;
    @Autowired
    protected SysConfigService sysConfigService;
    public void checkAppService(MchApp mchApp) {
        MchApp dbRecord = mchAppService.getById(mchApp.getAppId());
        if (dbRecord.getState()!=CS.NO){
            String accountAutoOff = sysConfigService.getDBApplicationConfig().getAccountAutoOff();
            if (isAccountOff(mchApp, Long.parseLong(accountAutoOff))){
                dbRecord.setState(CS.NO);
                mchAppService.updateById(dbRecord);
                log.error("符合自动关闭逻辑，应用：{}----被关闭", mchApp.getAppName());
                sendMessage(mchApp.getMchNo(),"符合自动关闭逻辑。\n应用："+mchApp.getAppName()+"----被关闭");
            }
        }
    }
    public boolean isAccountOff(MchApp mchApp, Long accountAutoOff){
        JSONObject paramJSON = new JSONObject();
        paramJSON.put("createdEnd",LocalDateTime.now().minusMinutes(Integer.parseInt(sysConfigService.getDBApplicationConfig().getCreatedEnd())));
        paramJSON.put("createdStart",LocalDateTime.now().minusMinutes(Integer.parseInt(sysConfigService.getDBApplicationConfig().getCreatedStart())));
        PayOrder payOrder = new PayOrder();
        boolean offAccount = false;
        payOrder.setAppId(mchApp.getAppId());
        LambdaQueryWrapper<PayOrder> wrapper = PayOrder.gw();
        IPage<PayOrder> pages = payOrderService.listByPage(new Page(1, -1), payOrder, paramJSON, wrapper);
        if (pages.getRecords().size() > accountAutoOff){
            offAccount = true;
            for (PayOrder order : pages.getRecords()) {
                if (order.getState() == PayOrder.STATE_SUCCESS) {
                    offAccount = false;
                    break;
                }
            }
        }
        return offAccount;
    }
    public void sendMessage(String mchNo, String messageText) {
        TelegramChat telegramChat = telegramChatService.queryTelegramChatByMchNo(mchNo);
        SendMessage message = new SendMessage();
        if (telegramChat == null){
            message.setChatId(sysConfigService.getDBApplicationConfig().getBotTelegramChatId());
        } else {
            message.setChatId(telegramChat.getChatId());
        }
        message.setText(messageText);
        try {
            myCustomBot.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public String payOrderCount(String mchNo){
        Date date = DateUtil.offsetDay(new Date(),-0).toJdkDate();
        String dayStart = DateUtil.beginOfDay(date).toString(DatePattern.NORM_DATETIME_MINUTE_PATTERN);
        String dayEnd = DateUtil.endOfDay(date).toString(DatePattern.NORM_DATETIME_MINUTE_PATTERN);
        // 每日交易金额查询
        Map dayAmount = payOrderService.payCount(null, PayOrder.STATE_SUCCESS, null, dayStart, dayEnd);
        Map dayAmountSuccess = payOrderService.payCountSuccess(mchNo, PayOrder.STATE_SUCCESS, null, dayStart, dayEnd);
        String todayAmount = "0.00";    // 今日金额
        String todayPayCount = "0";    // 今日交易笔数
        String todayAmountSuccess = "0.00";    // 今日完成金额
        String todayPayCountSuccess = "0";    // 今日完成交易笔数
        if (dayAmount != null) {
            todayAmount = dayAmount.get("payAmount").toString();
            todayPayCount = dayAmount.get("payCount").toString();
            todayAmountSuccess = dayAmountSuccess.get("payAmount").toString();
            todayPayCountSuccess = dayAmountSuccess.get("payCount").toString();
        }
        return "今日收款金额：￥"+todayAmount+"元\n"+
                "今日收款笔数："+todayPayCount+"笔\n"+
                "今日完成金额：￥"+todayAmountSuccess+"元\n"+
                "今日完成笔数："+todayPayCountSuccess+"笔\n";
    }


}
