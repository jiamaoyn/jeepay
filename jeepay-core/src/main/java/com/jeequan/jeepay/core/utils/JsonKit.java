package com.jeequan.jeepay.core.utils;

import com.alibaba.fastjson.JSONObject;

/*
 * json工具类
 * @date 2021/6/8 16:51
 */
public class JsonKit {

    public static JSONObject newJson(String key, Object val) {

        JSONObject result = new JSONObject();
        result.put(key, val);
        return result;
    }


}
