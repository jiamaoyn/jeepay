package com.test.ctrl.telegram;

import com.jeequan.jeepay.core.model.ApiRes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/api/ceshi")
public class TelegramQueryOrderController {
    @RequestMapping(value = "/info")
    private ApiRes<Object> toPayForm(HttpServletRequest request) {
        System.out.println(request.getRequestURI());
        System.out.println(request.getScheme());
        System.out.println(request.getServerName());
        String realIp = request.getHeader("X-Real-IP");
        String forwardedFor = request.getHeader("X-Forwarded-For");
        String protocol = request.getHeader("X-Forwarded-Proto");
        String Host = request.getHeader("Host");
        System.out.println(realIp + "---------" +forwardedFor + "---------"+protocol+ "---------"+Host);
        return ApiRes.ok(request);
    }
}
