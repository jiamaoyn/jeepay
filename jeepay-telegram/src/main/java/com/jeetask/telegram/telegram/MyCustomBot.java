package com.jeetask.telegram.telegram;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.TelegramChat;
import com.jeequan.jeepay.service.impl.PayOrderService;
import com.jeequan.jeepay.service.impl.SysConfigService;
import com.jeequan.jeepay.service.impl.TelegramChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

@Component
public class MyCustomBot extends TelegramLongPollingBot {

    private final SysConfigService sysConfigService;
    private final TelegramChatService telegramChatService;
    private final PayOrderService payOrderService;

    @Autowired
    public MyCustomBot(DefaultBotOptions options, SysConfigService sysConfigService, TelegramChatService telegramChatService, PayOrderService payOrderService) {
        super(options);
        this.sysConfigService = sysConfigService;
        this.telegramChatService = telegramChatService;
        this.payOrderService = payOrderService;
    }

    @Override
    public String getBotUsername() {
        return sysConfigService.getDBApplicationConfig().getBotTelegramUsername();
    }

    @Override
    public String getBotToken() {
        return sysConfigService.getDBApplicationConfig().getBotTelegramToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            System.out.println(messageText);
            String sendText = "";
            String chatId = String.valueOf(update.getMessage().getChatId());
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setReplyToMessageId(update.getMessage().getMessageId());
            if (messageText.startsWith("绑定M")){
                String[] parts = messageText.split("M");
                sendText = "绑定失败，商户号已被绑定或错误";
                if (parts.length > 1){
                    TelegramChat telegramChat = telegramChatService.createTelegramChat(chatId, "M" + parts[1]);
                    if (telegramChat != null){
                        sendText = "绑定成功";
                    }
                }
            } else if (messageText.equals("解绑")) {
                Boolean aBoolean = telegramChatService.deleteTelegramChat(chatId);
                if (aBoolean){
                    sendText = "解绑成功";
                } else {
                    sendText = "解绑失败";
                }
            } else if (messageText.equals("收款统计")) {
                TelegramChat telegramChat = telegramChatService.queryTelegramChatByChatId(chatId);
                System.out.println(telegramChat);
                System.out.println(sysConfigService.getDBApplicationConfig().getBotTelegramChatId());
                System.out.println(chatId);
                System.out.println(sysConfigService.getDBApplicationConfig().getBotTelegramChatId().equals(chatId));
                if (telegramChat == null && sysConfigService.getDBApplicationConfig().getBotTelegramChatId().equals(chatId)){
                    Date date = DateUtil.offsetDay(new Date(),-0).toJdkDate();
                    String dayStart = DateUtil.beginOfDay(date).toString(DatePattern.NORM_DATETIME_MINUTE_PATTERN);
                    String dayEnd = DateUtil.endOfDay(date).toString(DatePattern.NORM_DATETIME_MINUTE_PATTERN);
                    // 每日交易金额查询
                    Map dayAmount = payOrderService.payCount(null, PayOrder.STATE_SUCCESS, null, dayStart, dayEnd);
                    Map dayAmountSuccess = payOrderService.payCountSuccess(null, PayOrder.STATE_SUCCESS, null, dayStart, dayEnd);
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
                    sendText =  "今日收款金额：￥"+todayAmount+"元\n"+
                                "今日收款笔数："+todayPayCount+"笔\n"+
                                "今日完成金额：￥"+todayAmountSuccess+"元\n"+
                                "今日完成笔数："+todayPayCountSuccess+"笔\n";
                } else {
                    sendText = "收款统计";
                }
            }
            if (!sendText.isEmpty()){
                try {
                    message.setText(sendText);
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
