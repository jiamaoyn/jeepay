package com.jeepay.telegram.telegram;

import com.jeequan.jeepay.core.entity.TelegramChat;
import com.jeequan.jeepay.service.impl.SysConfigService;
import com.jeequan.jeepay.service.impl.TelegramChatService;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class MyCustomBot extends TelegramLongPollingBot {
    public final TelegramChatService telegramChatService;
    public SysConfigService sysConfigService;
    public MyCustomBot(DefaultBotOptions options, TelegramChatService telegramChatService, SysConfigService sysConfigService) {
        super(options);
        this.telegramChatService = telegramChatService;
        this.sysConfigService = sysConfigService;
    }

    @Override
    public String getBotUsername() {
        return sysConfigService.getDBApplicationConfig().getBotTelegramUsername(); // 替换为您的 用户名
    }

    @Override
    public String getBotToken() {
        return sysConfigService.getDBApplicationConfig().getBotTelegramToken(); // 替换为您的  Token
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message msg = update.getMessage();
        User user = msg.getFrom();
        Long id = user.getId();
        Integer messageId = msg.getMessageId();
        long chatId = msg.getChatId();
        String sendMsgText = "";
        TelegramChat telegramChat = telegramChatService.queryTelegramChat(String.valueOf(chatId));
        if (telegramChat == null){
            sendMsgText = "未绑定，请先绑定商户号。请发送：绑定+xxxxx";
        }
        sendText(chatId, messageId, sendMsgText);
        System.out.println("查询是否绑定商户号："+telegramChatService.queryTelegramChat(String.valueOf(chatId)));
        System.out.println("消息的发送者。i1d:"+id);
        System.out.println("消息所在的聊天。idchatId:"+chatId);
        System.out.println("消息的唯一messageId:"+messageId);
        System.out.println("消息的内容:"+msg.getText());
        System.out.println("消息的发送时间:"+msg.getDate());
    }
    public void sendMessageToChat(String chatId, String messageText) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageText);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    public void sendText(Long chatId,Integer messageId, String messageText){
        // 创建回复消息
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(messageText);
        message.setReplyToMessageId(messageId); // 设置这条消息作为回复的消息ID
        try {
            // 发送回复
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        DefaultBotOptions options = new DefaultBotOptions();
        options.setBaseUrl("https://apitelegram.aod.icu/bot"); // 设置自定义的 URL
        MyCustomBot bot = new MyCustomBot(options, null,null);
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
