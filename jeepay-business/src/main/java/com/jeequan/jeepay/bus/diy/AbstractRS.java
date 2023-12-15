package com.jeequan.jeepay.bus.diy;

import com.alibaba.fastjson.JSON;
import lombok.Data;

import java.io.Serializable;

/*
 * 接口抽象RS对象, 本身无需实例化
 * @date 2021/6/8 17:39
 */
@Data
public abstract class AbstractRS implements Serializable {

    public String toJSONString() {
        return JSON.toJSONString(this);
    }

}
