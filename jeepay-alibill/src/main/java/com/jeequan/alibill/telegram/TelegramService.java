package com.jeequan.alibill.telegram;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
public class TelegramService {

    private final MyCustomBot myCustomBot;

    public TelegramService(MyCustomBot myCustomBot) {
        this.myCustomBot = myCustomBot;
    }

    public void sendMessage(String chatId, String messageText) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageText);

        try {
            myCustomBot.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
