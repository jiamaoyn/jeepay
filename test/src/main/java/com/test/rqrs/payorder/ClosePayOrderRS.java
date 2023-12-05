package com.test.rqrs.payorder;

import com.alibaba.fastjson.annotation.JSONField;
import com.test.rqrs.AbstractRS;
import com.test.rqrs.msg.ChannelRetMsg;
import lombok.Data;

/*
 * 关闭订单 响应参数
 *
 * @author xiaoyu
 * @site https://www.jeequan.com
 * @date 2022/1/25 9:17
 */
@Data
public class ClosePayOrderRS extends AbstractRS {

    /**
     * 上游渠道返回数据包 (无需JSON序列化)
     **/
    @JSONField(serialize = false)
    private ChannelRetMsg channelRetMsg;

}
