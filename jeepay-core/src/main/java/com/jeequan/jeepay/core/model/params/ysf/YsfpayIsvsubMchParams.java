package com.jeequan.jeepay.core.model.params.ysf;

import com.jeequan.jeepay.core.model.params.IsvsubMchParams;
import lombok.Data;

/*
 * 云闪付 配置信息
 * @date 2021/6/8 18:02
 */
@Data
public class YsfpayIsvsubMchParams extends IsvsubMchParams {

    private String merId;   // 商户编号

}
