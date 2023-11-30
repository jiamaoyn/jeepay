package com.jeequan.alibill.telegram;

import org.springframework.stereotype.Service;

@Service
public class BotConfigService {

    // 添加数据库访问逻辑来获取这些值
    public String getBotUsername() {
        // 从数据库获取 Bot 的用户名
        return "your_bot_username";
    }

    public String getBotToken() {
        // 从数据库获取 Bot 的 Token
        return "6215593979:AAG9MPC4fRoIp--d1WS2fVij3aAe3FHi08s";
    }

    public String getBaseUrl() {
        // 从数据库获取 BaseUrl
        return "https://apitelegram.aod.icu/bot";
    }
}
