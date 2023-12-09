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
                sendMessage(mchApp.getMchNo(),mchApp.getAppName()+"----被关闭\n"+"该应用长时间没有支付成功订单,系统自动停止使用，请手动检查账号状态。\n若账号没有问题，请十分钟后启用");
                sendMessageSys(mchApp.getAppName()+"----被关闭\n"+"该应用长时间没有支付成功订单,系统自动停止使用，请手动检查账号状态。\n若账号没有问题，请十分钟后启用");
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
        if (telegramChat != null){
            message.setChatId(telegramChat.getChatId());
            message.setText(messageText);
            executeSendMessage(message);
        }
    }

    public void sendMessageSys(String messageText) {
        SendMessage message = new SendMessage();
        message.setChatId(sysConfigService.getDBApplicationConfig().getBotTelegramChatId());
        message.setText(messageText);
        executeSendMessage(message);
    }

    public void executeSendMessage(SendMessage message){
        try {
            myCustomBot.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


}
