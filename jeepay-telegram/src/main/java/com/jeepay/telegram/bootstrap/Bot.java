package com.jeepay.telegram.bootstrap;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

public class Bot extends TelegramLongPollingBot {
    public Bot(DefaultBotOptions botOptions) {
        super(botOptions);
    }
    @Override
  public String getBotUsername() {
      return "TutorialBot";
  }

  @Override
  public String getBotToken() {
      return "6215593979:AAG9MPC4fRoIp--d1WS2fVij3aAe3FHi08s";
  }
  @Override
  public void onUpdateReceived(Update update) {
      Message msg = update.getMessage();
      User user = msg.getFrom();

      System.out.println(user.getFirstName() + " wrote " + msg.getText());
  }
}