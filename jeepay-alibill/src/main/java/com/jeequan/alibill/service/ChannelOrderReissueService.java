package com.jeequan.alibill.service;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.domain.AccountLogItemResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jeequan.alibill.telegram.MyCustomBot;
import com.jeequan.alibill.telegram.TelegramService;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.ctrls.AbstractCtrl;
import com.jeequan.jeepay.core.entity.MchApp;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.TelegramChat;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.core.utils.SpringBeansUtil;
import com.jeequan.jeepay.service.impl.MchAppService;
import com.jeequan.jeepay.service.impl.PayOrderService;
import com.jeequan.alibill.channel.IPayOrderQueryService;
import com.jeequan.alibill.model.MchAppConfigContext;
import com.jeequan.jeepay.service.impl.SysConfigService;
import com.jeequan.jeepay.service.impl.TelegramChatService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/*
 * 查询上游订单， &  补单服务实现类
 * @date 2021/6/8 17:40
 */

@Service
@Slf4j
public class ChannelOrderReissueService extends AbstractCtrl {

    @Autowired
    private ConfigContextQueryService configContextQueryService;
    @Autowired
    private MyCustomBot myCustomBot;
    @Autowired
    private TelegramChatService telegramChatService;
    @Autowired
    private PayOrderService payOrderService;
    @Autowired
    private MchAppService mchAppService;
    @Autowired
    private PayOrderProcessService payOrderProcessService;
    @Autowired
    protected SysConfigService sysConfigService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;;
    public void processPayOrderBill(MchApp mchApp, Date startDate, Date endDate) {
        try {
            //查询支付接口是否存在
            IPayOrderQueryService queryService = SpringBeansUtil.getBean("alipayPayOrderQueryService", IPayOrderQueryService.class);
            // 支付通道接口实现不存在
            if (queryService == null) {
                log.error("{} interface not exists error!", "alipay");
                return;
            }
            String string = stringRedisTemplate.opsForValue().get("alipayAppId"+mchApp.getAppId());
            if (string != null && mchApp.getState() == CS.NO){
                return;
            }
            //查询出商户应用的配置信息
            MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(mchApp.getMchNo(), mchApp.getAppId());
            List<AccountLogItemResult> accountLogItemResultList = queryService.query(mchAppConfigContext, startDate, endDate);
            if (accountLogItemResultList == null) {
                String accountAutoOff = sysConfigService.getDBApplicationConfig().getAccountAutoOff();
                if (isAccountOff(mchApp, Long.parseLong(accountAutoOff))){
                    MchApp dbRecord = mchAppService.getById(mchAppConfigContext.getAppId());
                    if (dbRecord.getState()!=CS.NO){
                        dbRecord.setState(CS.NO);
                        mchAppService.updateById(dbRecord);
                        log.error("累计---{}---笔无成功订单，应用：{}----被关闭", accountAutoOff, mchApp.getAppName());
                        sendMessage(mchApp.getMchNo(),"累计---"+ accountAutoOff +"---笔无成功订单，应用："+mchApp.getAppName()+"----被关闭");
                    }
                }
                return;
            }
            accountLogItemResultList.forEach(accountLogItemResult -> {
                 if (accountLogItemResult.getTransMemo()!=null) {
                    PayOrder payOrder = payOrderService.queryPayOrderIdNoStateIng(accountLogItemResult.getTransMemo());
                    if (payOrder == null){
                        return;
                    }
                    System.out.println("查找订单号："+accountLogItemResult.getTransMemo()+"查找订单号结果"+payOrder);
                    if (Long.parseLong(AmountUtil.convertDollar2Cent(accountLogItemResult.getTransAmount())) == payOrder.getAmount()){
                        if (payOrderService.updateIng2Success(payOrder.getPayOrderId(), accountLogItemResult.getAlipayOrderNo(), null)) {
                            //订单支付成功，其他业务逻辑
                            payOrderProcessService.confirmSuccessPolling(payOrder);
                            log.info("订单支付成功[{}],alipay_order_no:{},balance:{},trans_amount:{},direction:{},trans_dt:{},trans_memo:{}" ,
                                    payOrder.getPayOrderId(),accountLogItemResult.getAlipayOrderNo(),accountLogItemResult.getBalance(),accountLogItemResult.getTransAmount(),accountLogItemResult.getDirection(),accountLogItemResult.getTransDt(),accountLogItemResult.getTransMemo());
                        }
                    }
                }

            });
        } catch (Exception e) {  //继续下一次迭代查询
            log.error("error appid:{} 支付宝商家订单回调",mchApp.getAppId(), e);
            MchApp dbRecord = mchAppService.getById(mchApp.getAppId());
            if (dbRecord.getState()!=CS.NO){
                dbRecord.setState(CS.NO);
                mchAppService.updateById(dbRecord);
                log.error("出现异常，应用："+mchApp.getAppName()+"----被关闭。请登陆后台查看，如错误关闭，请重新打开");
                sendMessage(mchApp.getMchNo(),"出现异常，应用："+mchApp.getAppName()+"----被关闭。\n请登陆后台查看，如错误关闭，请重新打开");
            } else {
                stringRedisTemplate.opsForValue().set("alipayAppId"+mchApp.getAppId(), "yourValue", 3, TimeUnit.MINUTES);
            }
        }
    }
    public boolean isAccountOff(MchApp mchApp, Long accountAutoOff){
        PayOrder payOrder = new PayOrder();
        boolean offAccount = false;
        payOrder.setAppId(mchApp.getAppId());
        LambdaQueryWrapper<PayOrder> wrapper = PayOrder.gw();
        IPage<PayOrder> pages = payOrderService.listByPage(new Page(1, accountAutoOff), payOrder, null, wrapper);
        if (pages.getRecords().size() == accountAutoOff){
            offAccount = true;
            for (PayOrder order : pages.getRecords()) {
                if (order.getState() == PayOrder.STATE_SUCCESS) {
                    offAccount = false;
                    break;
                }
            }
        }
        return offAccount;
    }
    public void sendMessage(String mchNo, String messageText) {
        TelegramChat telegramChat = telegramChatService.queryTelegramChatByMchNo(mchNo);
        SendMessage message = new SendMessage();
        if (telegramChat == null){
            message.setChatId(sysConfigService.getDBApplicationConfig().getBotTelegramChatId());
        } else {
            message.setChatId(telegramChat.getChatId());
        }
        message.setText(messageText);
        try {
            myCustomBot.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


}
