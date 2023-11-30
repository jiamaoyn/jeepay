package com.jeequan.alibill.telegram;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.annotation.PostConstruct;

@Component
public class BotInitializer {

    private final MyCustomBot myCustomBot;

    @Autowired
    public BotInitializer(MyCustomBot myCustomBot) {
        this.myCustomBot = myCustomBot;
    }

    @PostConstruct
    public void init() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(myCustomBot);
        } catch (TelegramApiException e) {
            e.printStackTrace(); // Handle the exception properly
        }
    }
}
