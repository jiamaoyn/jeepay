package com.jeetask.telegram.telegram;

import com.jeequan.jeepay.core.entity.TelegramChat;
import com.jeequan.jeepay.service.impl.PayOrderService;
import com.jeequan.jeepay.service.impl.SysConfigService;
import com.jeequan.jeepay.service.impl.TelegramChatService;
import com.jeetask.telegram.service.TelegramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class MyCustomBot extends TelegramLongPollingBot {

    private final SysConfigService sysConfigService;
    private final TelegramChatService telegramChatService;
    private final TelegramService telegramService;

    @Autowired
    public MyCustomBot(DefaultBotOptions options, SysConfigService sysConfigService, TelegramChatService telegramChatService, TelegramService telegramService) {
        super(options);
        this.sysConfigService = sysConfigService;
        this.telegramChatService = telegramChatService;
        this.telegramService = telegramService;
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
                if (telegramChat != null){
                    sendText =  telegramService.payOrderCount(telegramChat.getMchNo());
                } else if (sysConfigService.getDBApplicationConfig().getBotTelegramChatId().equals(chatId)) {
                    sendText =  telegramService.payOrderCount(null);
                } else {
                    sendText = "暂未绑定商户号，请绑定";
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
