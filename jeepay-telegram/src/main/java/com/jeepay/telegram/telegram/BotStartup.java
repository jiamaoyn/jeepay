package com.jeepay.telegram.telegram;

import com.jeequan.jeepay.service.impl.SysConfigService;
import com.jeequan.jeepay.service.impl.TelegramChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.annotation.PostConstruct;

@Component
public class BotStartup {
    @Autowired
    public TelegramChatService telegramChatService;
    @Autowired
    public SysConfigService sysConfigService;
    @PostConstruct
    public void startBot() {
        try {
            String apiBaseUrl = "https://apitelegram.aod.icu/bot";
            DefaultBotOptions options = new DefaultBotOptions();
            options.setBaseUrl(apiBaseUrl);
            MyCustomBot bot = new MyCustomBot(options,telegramChatService,sysConfigService);
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);
        } catch (Exception e) {
            // 异常处理逻辑
            e.printStackTrace();
        }
    }
}