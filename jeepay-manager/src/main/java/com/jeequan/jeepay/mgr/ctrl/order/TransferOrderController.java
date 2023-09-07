package com.jeequan.jeepay.mgr.ctrl.order;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.entity.TransferOrder;
import com.jeequan.jeepay.core.model.ApiPageRes;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.TransferOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * 转账订单api
 *
 * @date 2021/8/13 10:52
 */
@Api(tags = "订单管理（转账类）")
@RestController
@RequestMapping("/api/transferOrders")
public class TransferOrderController extends CommonCtrl {

    @Autowired
    private TransferOrderService transferOrderService;

    /**
     * list
     **/
    @ApiOperation("转账订单信息列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "pageNumber", value = "分页页码", dataType = "int", defaultValue = "1"),
            @ApiImplicitParam(name = "pageSize", value = "分页条数", dataType = "int", defaultValue = "20"),
            @ApiImplicitParam(name = "createdStart", value = "日期格式字符串（yyyy-MM-dd HH:mm:ss），时间范围查询--开始时间，查询范围：大于等于此时间"),
            @ApiImplicitParam(name = "createdEnd", value = "日期格式字符串（yyyy-MM-dd HH:mm:ss），时间范围查询--结束时间，查询范围：小于等于此时间"),
            @ApiImplicitParam(name = "mchNo", value = "商户号"),
            @ApiImplicitParam(name = "unionOrderId", value = "转账/商户/渠道订单号"),
            @ApiImplicitParam(name = "appId", value = "应用ID"),
            @ApiImplicitParam(name = "state", value = "支付状态: 0-订单生成, 1-转账中, 2-转账成功, 3-转账失败, 4-订单关闭", dataType = "Byte")
    })
    @PreAuthorize("hasAuthority('ENT_TRANSFER_ORDER_LIST')")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ApiPageRes<TransferOrder> list() {

        TransferOrder transferOrder = getObject(TransferOrder.class);
        JSONObject paramJSON = getReqParamJSON();
        LambdaQueryWrapper<TransferOrder> wrapper = TransferOrder.gw();
        IPage<TransferOrder> pages = transferOrderService.pageList(getIPage(), wrapper, transferOrder, paramJSON);

        return ApiPageRes.pages(pages);
    }

    /**
     * detail
     **/
    @ApiOperation("转账订单信息详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "recordId", value = "转账订单号", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_TRANSFER_ORDER_VIEW')")
    @RequestMapping(value = "/{recordId}", method = RequestMethod.GET)
    public ApiRes detail(@PathVariable("recordId") String transferId) {
        TransferOrder refundOrder = transferOrderService.getById(transferId);
        if (refundOrder == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }
        return ApiRes.ok(refundOrder);
    }
}
