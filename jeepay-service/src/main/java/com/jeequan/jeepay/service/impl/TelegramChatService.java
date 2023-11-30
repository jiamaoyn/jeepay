package com.jeequan.jeepay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.TelegramChat;
import com.jeequan.jeepay.service.mapper.TelegramChatMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class TelegramChatService extends ServiceImpl<TelegramChatMapper, TelegramChat> {
    public TelegramChat queryTelegramChat(String chatId) {
        if (StringUtils.isNotEmpty(chatId)) {
            return getOne(TelegramChat.gw().eq(TelegramChat::getChatId, chatId));
        }else {
            return null;
        }
    }
}