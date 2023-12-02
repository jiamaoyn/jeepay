package com.jeequan.alibill.telegram;

import com.jeequan.jeepay.service.impl.SysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.DefaultBotOptions;

@Configuration
public class BotInitializerConfig {

    @Autowired
    private SysConfigService sysConfigService;

    @Bean
    public DefaultBotOptions botOptions() {
        DefaultBotOptions options = new DefaultBotOptions();
        options.setBaseUrl(sysConfigService.getDBApplicationConfig().getBotTelegramUrl());
        return options;
    }
}
