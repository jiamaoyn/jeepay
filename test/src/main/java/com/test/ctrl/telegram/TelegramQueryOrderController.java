package com.test.ctrl.telegram;

import com.jeequan.jeepay.core.model.ApiRes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/telegram")
public class TelegramQueryOrderController {
    @RequestMapping(value = "/orderNch/{mchOrderNo}/{channelOrderNo}")
    private ApiRes<Object> toPayForm(@PathVariable("mchOrderNo") String mchOrderNo, @PathVariable("channelOrderNo") String channelOrderNo) {
        return ApiRes.ok("不是商家账单，无法回调");
    }
}
