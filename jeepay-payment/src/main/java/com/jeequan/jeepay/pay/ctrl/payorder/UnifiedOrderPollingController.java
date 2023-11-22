package com.jeequan.jeepay.pay.ctrl.payorder;

import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayWay;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRS;
import com.jeequan.jeepay.pay.service.ConfigContextQueryService;
import com.jeequan.jeepay.service.impl.PayWayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 统一下单 controller
 */
@Slf4j
@RestController
public class UnifiedOrderPollingController extends AbstractPayOrderController {

    @Autowired
    private PayWayService payWayService;
    @Autowired
    private ConfigContextQueryService configContextQueryService;

    /**
     * 统一下单接口
     **/
    @PostMapping("/api/pay/unifiedOrderPolling")
    public ApiRes unifiedOrderPolling(HttpServletRequest request) {

        //获取参数 & 验签
        UnifiedOrderRQ rq = getRQByWithMchSignPolling(UnifiedOrderRQ.class);

        UnifiedOrderRQ bizRQ = buildBizRQ(rq);
        //实现子类的res
        ApiRes apiRes = unifiedOrderPolling(bizRQ.getWayCode(), bizRQ, request);
        if (apiRes.getData() == null) {
            return apiRes;
        }

        UnifiedOrderRS bizRes = (UnifiedOrderRS) apiRes.getData();

        //聚合接口，返回的参数
        UnifiedOrderRS res = new UnifiedOrderRS();
        BeanUtils.copyProperties(bizRes, res);

        //只有 订单生成（QR_CASHIER） || 支付中 || 支付成功返回该数据
        if (bizRes.getOrderState() != null && (bizRes.getOrderState() == PayOrder.STATE_INIT || bizRes.getOrderState() == PayOrder.STATE_ING || bizRes.getOrderState() == PayOrder.STATE_SUCCESS)) {
            res.setPayDataType(bizRes.buildPayDataType());
            res.setPayData(bizRes.buildPayData());
        }

        return ApiRes.okWithSign(res, configContextQueryService.queryMchInfo(rq.getMchNo()).getSecret());
    }


    private UnifiedOrderRQ buildBizRQ(UnifiedOrderRQ rq) {

        //支付方式  比如： ali_bar
        String wayCode = rq.getWayCode();

        if (payWayService.count(PayWay.gw().eq(PayWay::getWayCode, wayCode)) <= 0) {
            throw new BizException("不支持的支付方式");
        }

        //转换为 bizRQ
        return rq.buildBizRQ();
    }


}

