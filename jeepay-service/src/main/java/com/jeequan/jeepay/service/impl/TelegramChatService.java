package com.jeequan.jeepay.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.TelegramChat;
import com.jeequan.jeepay.service.mapper.TelegramChatMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
public class TelegramChatService extends ServiceImpl<TelegramChatMapper, TelegramChat> {
    private final MchInfoService mchInfoService;
    private final PayOrderService payOrderService;

    public TelegramChatService(MchInfoService mchInfoService, PayOrderService payOrderService) {
        this.mchInfoService = mchInfoService;
        this.payOrderService = payOrderService;
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
    public String payOrderCount(String mchNo){
        Date date = DateUtil.offsetDay(new Date(),-0).toJdkDate();
        String dayStart = DateUtil.beginOfDay(date).toString(DatePattern.NORM_DATETIME_MINUTE_PATTERN);
        String dayEnd = DateUtil.endOfDay(date).toString(DatePattern.NORM_DATETIME_MINUTE_PATTERN);
        // 每日交易金额查询
        Map dayAmount = payOrderService.payCount(null, PayOrder.STATE_SUCCESS, null, dayStart, dayEnd);
        Map dayAmountSuccess = payOrderService.payCountSuccess(mchNo, PayOrder.STATE_SUCCESS, null, dayStart, dayEnd);
        String todayAmount = "0.00";    // 今日金额
        String todayPayCount = "0";    // 今日交易笔数
        String todayAmountSuccess = "0.00";    // 今日完成金额
        String todayPayCountSuccess = "0";    // 今日完成交易笔数
        if (dayAmount != null) {
            todayAmount = dayAmount.get("payAmount").toString();
            todayPayCount = dayAmount.get("payCount").toString();
            todayAmountSuccess = dayAmountSuccess.get("payAmount").toString();
            todayPayCountSuccess = dayAmountSuccess.get("payCount").toString();
        }
        return "今日收款金额：￥"+todayAmount+"元\n"+
                "今日收款笔数："+todayPayCount+"笔\n"+
                "今日完成金额：￥"+todayAmountSuccess+"元\n"+
                "今日完成笔数："+todayPayCountSuccess+"笔\n";
    }
}