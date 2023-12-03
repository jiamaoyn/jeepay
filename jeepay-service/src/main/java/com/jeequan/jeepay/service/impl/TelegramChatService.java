package com.jeequan.jeepay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.entity.MchPayPassage;
import com.jeequan.jeepay.core.entity.TelegramChat;
import com.jeequan.jeepay.service.mapper.TelegramChatMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class TelegramChatService extends ServiceImpl<TelegramChatMapper, TelegramChat> {
    private final MchInfoService mchInfoService;

    public TelegramChatService(MchInfoService mchInfoService) {
        this.mchInfoService = mchInfoService;
    }

    public TelegramChat queryTelegramChat(String chatId) {
        if (StringUtils.isNotEmpty(chatId)) {
            return getOne(TelegramChat.gw().eq(TelegramChat::getChatId, chatId));
        }else {
            return null;
        }
    }
    public TelegramChat queryTelegramChatByMchNo(String mchNo) {
        if (StringUtils.isNotEmpty(mchNo)) {
            return getOne(TelegramChat.gw().eq(TelegramChat::getMchNo, mchNo));
        }else {
            return null;
        }
    }
    public TelegramChat queryTelegramChatByChatId(String chatId) {
        if (StringUtils.isNotEmpty(chatId)) {
            return getOne(TelegramChat.gw().eq(TelegramChat::getChatId, chatId));
        }else {
            return null;
        }
    }
    public Boolean deleteTelegramChat(String chatId){
        if (StringUtils.isNotEmpty(chatId) || queryTelegramChat(chatId) == null) {
            this.remove(TelegramChat.gw().eq(TelegramChat::getChatId, chatId));
            return true;
        }else {
            return false;
        }
    }
    public TelegramChat createTelegramChat(String chatId,String mchNo) {
        System.out.println(mchNo);
        System.out.println(StringUtils.isNotEmpty(chatId) || StringUtils.isNotEmpty(mchNo) || queryTelegramChat(chatId) != null);
        if (StringUtils.isNotEmpty(chatId) || StringUtils.isNotEmpty(mchNo) || queryTelegramChat(chatId) != null) {
            MchInfo oneByMch = mchInfoService.selectById(mchNo);
            if (oneByMch == null || oneByMch.getState() == CS.NO){
                return null;
            }
            TelegramChat telegramChat = new TelegramChat();
            telegramChat.setChatId(chatId);
            telegramChat.setMchNo(mchNo);
            telegramChat.setState((byte) 1);
            this.save(telegramChat);
            return telegramChat;
        }else {
            return null;
        }
    }
}