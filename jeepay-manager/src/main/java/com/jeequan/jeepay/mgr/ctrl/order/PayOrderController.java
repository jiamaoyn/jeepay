package com.jeequan.jeepay.mgr.ctrl.order;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.JeepayClient;
import com.jeequan.jeepay.components.mq.model.PayOrderMchNotifyMQ;
import com.jeequan.jeepay.components.mq.vender.IMQSender;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.entity.MchApp;
import com.jeequan.jeepay.core.entity.MchNotifyRecord;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayWay;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiPageRes;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.utils.JeepayKit;
import com.jeequan.jeepay.core.utils.SeqKit;
import com.jeequan.jeepay.core.utils.StringKit;
import com.jeequan.jeepay.exception.JeepayException;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.mgr.diy.QueryPayOrderRS;
import com.jeequan.jeepay.model.RefundOrderCreateReqModel;
import com.jeequan.jeepay.request.RefundOrderCreateRequest;
import com.jeequan.jeepay.response.RefundOrderCreateResponse;
import com.jeequan.jeepay.service.impl.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付订单类
 *
 * @date 2021-06-07 07:15
 */
@Api(tags = "订单管理（支付类）")
@RestController
@RequestMapping("/api/payOrder")
public class PayOrderController extends CommonCtrl {
    @Autowired
    private MchNotifyRecordService mchNotifyRecordService;
    @Autowired
    private MchInfoService mchInfoService;
    @Autowired
    private IMQSender mqSender;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private PayOrderService payOrderService;
    @Autowired
    private PayWayService payWayService;
    @Autowired
    private SysConfigService sysConfigService;
    @Autowired
    private MchAppService mchAppService;

    /**
     * 订单信息列表
     */
    @ApiOperation("支付订单信息列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "pageNumber", value = "分页页码", dataType = "int", defaultValue = "1"),
            @ApiImplicitParam(name = "pageSize", value = "分页条数", dataType = "int", defaultValue = "20"),
            @ApiImplicitParam(name = "createdStart", value = "日期格式字符串（yyyy-MM-dd HH:mm:ss），时间范围查询--开始时间，查询范围：大于等于此时间"),
            @ApiImplicitParam(name = "createdEnd", value = "日期格式字符串（yyyy-MM-dd HH:mm:ss），时间范围查询--结束时间，查询范围：小于等于此时间"),
            @ApiImplicitParam(name = "mchNo", value = "商户号"),
            @ApiImplicitParam(name = "unionOrderId", value = "支付/商户/渠道订单号"),
            @ApiImplicitParam(name = "isvNo", value = "服务商号"),
            @ApiImplicitParam(name = "appId", value = "应用ID"),
            @ApiImplicitParam(name = "wayCode", value = "支付方式代码"),
            @ApiImplicitParam(name = "state", value = "支付状态: 0-订单生成, 1-支付中, 2-支付成功, 3-支付失败, 4-已撤销, 5-已退款, 6-订单关闭", dataType = "Byte"),
            @ApiImplicitParam(name = "notifyState", value = "向下游回调状态, 0-未发送,  1-已发送"),
            @ApiImplicitParam(name = "divisionState", value = "0-未发生分账, 1-等待分账任务处理, 2-分账处理中, 3-分账任务已结束(不体现状态)")
    })
    @PreAuthorize("hasAuthority('ENT_ORDER_LIST')")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ApiPageRes<PayOrder> list() {
        PayOrder payOrder = getObject(PayOrder.class);
        JSONObject paramJSON = getReqParamJSON();
        LambdaQueryWrapper<PayOrder> wrapper = PayOrder.gw();
        IPage<PayOrder> pages = payOrderService.listByPage(getIPage(), payOrder, paramJSON, wrapper);
        // 得到所有支付方式
        Map<String, String> payWayNameMap = new HashMap<>();
        List<PayWay> payWayList = payWayService.list();
        for (PayWay payWay : payWayList) {
            payWayNameMap.put(payWay.getWayCode(), payWay.getWayName());
        }
        Map<String, String> payMchAppName = new HashMap<>();
        List<MchApp> payMchAppNameList = mchAppService.list();
        for (MchApp mchApp : payMchAppNameList) {
            payMchAppName.put(mchApp.getAppId(), mchApp.getAppName());
        }
        for (PayOrder order : pages.getRecords()) {
            // 存入支付方式名称
            if (StringUtils.isNotEmpty(payMchAppName.get(order.getAppId()))) {
                order.addExt("mchAppName", payMchAppName.get(order.getAppId()));
            } else {
                order.addExt("mchAppName", "已删除或关闭");
            }
            if (StringUtils.isNotEmpty(payWayNameMap.get(order.getWayCode()))) {
                order.addExt("wayName", payWayNameMap.get(order.getWayCode()));
            } else {
                order.addExt("wayName", order.getWayCode());
            }
        }
        return ApiPageRes.pages(pages);
    }

    /**
     * @author: pangxiaoyu
     * @date: 2021/6/7 16:15
     * @describe: 支付订单信息
     */
    @ApiOperation("支付订单信息详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "payOrderId", value = "支付订单号", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_PAY_ORDER_VIEW')")
    @RequestMapping(value = "/{payOrderId}", method = RequestMethod.GET)
    public ApiRes detail(@PathVariable("payOrderId") String payOrderId) {
        PayOrder payOrder = payOrderService.getById(payOrderId);
        if (payOrder == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }
        return ApiRes.ok(payOrder);
    }
    @ApiOperation("手动回调支付订单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "payOrderId", value = "支付订单号", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_PAY_ORDER_NOTIFY')")
    @RequestMapping(value = "detailNotify/{payOrderId}", method = RequestMethod.GET)
    public ApiRes detailNotify(@PathVariable("payOrderId") String payOrderId) {
        PayOrder payOrder = payOrderService.getById(payOrderId);
        System.out.println("手动回调订单:"+payOrderId);
        if (payOrder == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }
        payOrderService.updateIng2SuccessDiy(payOrderId);
        payOrder = payOrderService.getById(payOrder.getPayOrderId());
        payOrder.setState(PayOrder.STATE_SUCCESS);
        payOrderNotifyPolling(payOrder);
        return ApiRes.ok();
    }

    /**
     * 商户通知信息， 只有订单是终态，才会发送通知， 如明确成功和明确失败
     **/
    public void payOrderNotify(PayOrder dbPayOrder) {

        try {
            // 通知地址为空
            if (StringUtils.isEmpty(dbPayOrder.getNotifyUrl())) {
                return;
            }

            //获取到通知对象
            MchNotifyRecord mchNotifyRecord = mchNotifyRecordService.findByPayOrder(dbPayOrder.getPayOrderId());

            if (mchNotifyRecord != null) {
                return;
            }

            //商户app私钥
            String appSecret = mchAppService.getOneByMch(dbPayOrder.getMchNo(), dbPayOrder.getAppId()).getAppSecret();
            // 封装通知url
            String notifyUrl = createNotifyUrl(dbPayOrder, appSecret);
            mchNotifyRecord = new MchNotifyRecord();
            mchNotifyRecord.setOrderId(dbPayOrder.getPayOrderId());
            mchNotifyRecord.setOrderType(MchNotifyRecord.TYPE_PAY_ORDER);
            mchNotifyRecord.setMchNo(dbPayOrder.getMchNo());
            mchNotifyRecord.setMchOrderNo(dbPayOrder.getMchOrderNo()); //商户订单号
            mchNotifyRecord.setIsvNo(dbPayOrder.getIsvNo());
            mchNotifyRecord.setAppId(dbPayOrder.getAppId());
            mchNotifyRecord.setNotifyUrl(notifyUrl);
            mchNotifyRecord.setResResult("");
            mchNotifyRecord.setNotifyCount(0);
            mchNotifyRecord.setState(MchNotifyRecord.STATE_ING); // 通知中

            try {
                mchNotifyRecordService.save(mchNotifyRecord);
            } catch (Exception e) {
                return;
            }

            //推送到MQ
            Long notifyId = mchNotifyRecord.getNotifyId();
            mqSender.send(PayOrderMchNotifyMQ.build(notifyId));

        } catch (Exception ignored) {
        }
    }
    public String createNotifyUrl(PayOrder payOrder, String appSecret) {

        QueryPayOrderRS queryPayOrderRS = QueryPayOrderRS.buildByPayOrder(payOrder);
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(queryPayOrderRS);
        jsonObject.put("reqTime", System.currentTimeMillis()); //添加请求时间
        // 报文签名
        jsonObject.put("sign", JeepayKit.getSign(jsonObject, appSecret));

        // 生成通知
        return StringKit.appendUrlQuery(payOrder.getNotifyUrl(), jsonObject);
    }
    /**
     * 商户通知信息， 只有订单是终态，才会发送通知， 如明确成功和明确失败
     **/
    public void payOrderNotifyPolling(PayOrder dbPayOrder) {

        try {
            // 通知地址为空
            if (StringUtils.isEmpty(dbPayOrder.getNotifyUrl())) {
                return;
            }

            //获取到通知对象
            MchNotifyRecord mchNotifyRecord = mchNotifyRecordService.findByPayOrder(dbPayOrder.getPayOrderId());
            if (mchNotifyRecord != null) {
                return;
            }

            //商户app私钥
            String appSecret = mchInfoService.getOneByMch(dbPayOrder.getMchNo()).getSecret();
            // 封装通知url
            String notifyUrl = createNotifyUrl(dbPayOrder, appSecret);
            mchNotifyRecord = new MchNotifyRecord();
            mchNotifyRecord.setOrderId(dbPayOrder.getPayOrderId());
            mchNotifyRecord.setOrderType(MchNotifyRecord.TYPE_PAY_ORDER);
            mchNotifyRecord.setMchNo(dbPayOrder.getMchNo());
            mchNotifyRecord.setMchOrderNo(dbPayOrder.getMchOrderNo()); //商户订单号
            mchNotifyRecord.setIsvNo(dbPayOrder.getIsvNo());
            mchNotifyRecord.setAppId(dbPayOrder.getAppId());
            mchNotifyRecord.setNotifyUrl(notifyUrl);
            mchNotifyRecord.setResResult("");
            mchNotifyRecord.setNotifyCount(0);
            mchNotifyRecord.setState(MchNotifyRecord.STATE_ING); // 通知中

            try {
                mchNotifyRecordService.save(mchNotifyRecord);
            } catch (Exception e) {
                return;
            }

            //推送到MQ
            Long notifyId = mchNotifyRecord.getNotifyId();
            mqSender.send(PayOrderMchNotifyMQ.build(notifyId));

        } catch (Exception ignored) {
        }
    }
    /**
     * 发起订单退款
     *
     * @author terrfly
     * @site https://www.jeequan.com
     * @date 2021/6/17 16:38
     */
    @ApiOperation("发起订单退款")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "payOrderId", value = "支付订单号", required = true),
            @ApiImplicitParam(name = "refundAmount", value = "退款金额", required = true),
            @ApiImplicitParam(name = "refundReason", value = "退款原因", required = true)
    })
    @MethodLog(remark = "发起订单退款")
    @PreAuthorize("hasAuthority('ENT_PAY_ORDER_REFUND')")
    @PostMapping("/refunds/{payOrderId}")
    public ApiRes refund(@PathVariable("payOrderId") String payOrderId) {

        Long refundAmount = getRequiredAmountL("refundAmount");
        String refundReason = getValStringRequired("refundReason");

        PayOrder payOrder = payOrderService.getById(payOrderId);
        if (payOrder == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }

        if (payOrder.getState() != PayOrder.STATE_SUCCESS) {
            throw new BizException("订单状态不正确");
        }

        if (payOrder.getRefundAmount() + refundAmount > payOrder.getAmount()) {
            throw new BizException("退款金额超过订单可退款金额！");
        }


        RefundOrderCreateRequest request = new RefundOrderCreateRequest();
        RefundOrderCreateReqModel model = new RefundOrderCreateReqModel();
        request.setBizModel(model);

        model.setMchNo(payOrder.getMchNo());     // 商户号
        model.setAppId(payOrder.getAppId());
        model.setPayOrderId(payOrderId);
        model.setMchRefundNo(SeqKit.genMhoOrderId());
        model.setRefundAmount(refundAmount);
        model.setRefundReason(refundReason);
        model.setCurrency("CNY");

        MchApp mchApp = mchAppService.getById(payOrder.getAppId());

        JeepayClient jeepayClient = new JeepayClient(sysConfigService.getDBApplicationConfig().getPaySiteUrl(), mchApp.getAppSecret());

        try {
            RefundOrderCreateResponse response = jeepayClient.execute(request);
            if (response.getCode() != 0) {
                throw new BizException(response.getMsg());
            }
            return ApiRes.ok(response.get());
        } catch (JeepayException e) {
            throw new BizException(e.getMessage());
        }
    }

}
