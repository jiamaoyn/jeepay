package com.jeequan.task.channel.alipay;

import com.alipay.api.domain.AlipayTradeCloseModel;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.task.channel.IPayOrderCloseService;
import com.jeequan.task.model.MchAppConfigContext;
import com.jeequan.task.rqrs.msg.ChannelRetMsg;
import com.jeequan.task.service.ConfigContextQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 支付宝 关闭订单接口实现类
 *
 * @author xiaoyu
 * @site https://www.jeequan.com
 * @date 2022/1/25 11:17
 */
@Service
public class AlipayPayOrderCloseService implements IPayOrderCloseService {

    @Autowired
    private ConfigContextQueryService configContextQueryService;

    @Override
    public String getIfCode() {
        return CS.IF_CODE.ALIPAY;
    }

    @Override
    public ChannelRetMsg close(PayOrder payOrder, MchAppConfigContext mchAppConfigContext) {

        AlipayTradeCloseRequest req = new AlipayTradeCloseRequest();

        // 商户订单号，商户网站订单系统中唯一订单号，必填
        AlipayTradeCloseModel model = new AlipayTradeCloseModel();
        model.setOutTradeNo(payOrder.getPayOrderId());
        req.setBizModel(model);

        //通用字段
        AlipayKit.putApiIsvInfo(mchAppConfigContext, req, model);

        AlipayTradeCloseResponse resp = configContextQueryService.getAlipayClientWrapper(mchAppConfigContext).execute(req);

        // 返回状态成功
        if (resp.isSuccess()) {
            return ChannelRetMsg.confirmSuccess(resp.getTradeNo());
        } else {
            return ChannelRetMsg.sysError(resp.getSubMsg());
        }
    }


}
