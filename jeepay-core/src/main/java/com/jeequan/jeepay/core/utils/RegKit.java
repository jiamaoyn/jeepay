package com.jeequan.jeepay.core.utils;

/*
 *
 * 正则验证kit
 * @author terrfly
 * @site https://www.jeequan.com
 * @date 2021/6/8 16:56
 */
public class RegKit {

    public static final String REG_MOBILE = "^1\\d{10}$"; //判断是否是手机号
    public static final String REG_ALIPAY_USER_ID = "^2088\\d{12}$"; //判断是支付宝用户Id 以2088开头的纯16位数字

    public static boolean isMobile(String str) {
        return match(str, REG_MOBILE);
    }

    public static boolean isAlipayUserId(String str) {
        return match(str, REG_ALIPAY_USER_ID);
    }


    /**
     * 正则验证
     */
    public static boolean match(String text, String reg) {
        if (text == null) {
            return false;
        }
        return text.matches(reg);
    }


}
