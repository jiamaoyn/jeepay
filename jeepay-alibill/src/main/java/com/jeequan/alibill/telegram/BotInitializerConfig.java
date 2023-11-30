package com.jeequan.alibill.telegram;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.DefaultBotOptions;

@Configuration
public class BotInitializerConfig {

    @Autowired
    private BotConfigService botConfigService;

    @Bean
    public DefaultBotOptions botOptions() {
        DefaultBotOptions options = new DefaultBotOptions();
        options.setBaseUrl(botConfigService.getBaseUrl());
        return options;
    }
}
