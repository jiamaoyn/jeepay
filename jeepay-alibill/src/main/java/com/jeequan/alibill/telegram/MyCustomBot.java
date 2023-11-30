package com.jeequan.alibill.telegram;

import com.jeequan.jeepay.core.entity.TelegramChat;
import com.jeequan.jeepay.service.impl.TelegramChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class MyCustomBot extends TelegramLongPollingBot {

    private final BotConfigService botConfigService;
    private final TelegramChatService telegramChatService;

    @Autowired
    public MyCustomBot(DefaultBotOptions options, BotConfigService botConfigService, TelegramChatService telegramChatService) {
        super(options);
        this.botConfigService = botConfigService;
        this.telegramChatService = telegramChatService;
    }

    @Override
    public String getBotUsername() {
        return botConfigService.getBotUsername();
    }

    @Override
    public String getBotToken() {
        return botConfigService.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
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
                sendText = "收款统计";
            }
//            TelegramChat telegramChat = telegramChatService.queryTelegramChat(chatId);
//            if (telegramChat == null){
//                messageText = "商户号未绑定，请先绑定商户号。\n绑定命令：绑定+商户号\n例如：绑定+M111111111";
//            } else {
//                messageText = "有什么可以帮你";
//            }
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
