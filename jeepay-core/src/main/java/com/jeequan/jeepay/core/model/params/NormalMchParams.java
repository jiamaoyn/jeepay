package com.jeequan.jeepay.core.model.params;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;

/*
 * 抽象类 普通商户参数定义
 * @date 2021/6/8 16:33
 */
public abstract class NormalMchParams {

    public static NormalMchParams factory(String ifCode, String paramsStr) {

        try {
            return (NormalMchParams) JSONObject.parseObject(paramsStr, Class.forName(NormalMchParams.class.getPackage().getName() + "." + ifCode + "." + StrUtil.upperFirst(ifCode) + "NormalMchParams"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 敏感数据脱敏
     */
    public abstract String deSenData();

}
