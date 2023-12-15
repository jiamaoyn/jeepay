package com.jeequan.jeepay.bus.ctrl.merchant;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.entity.SysUser;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.bus.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.MchInfoService;
import com.jeequan.jeepay.service.impl.PayOrderService;
import com.jeequan.jeepay.service.impl.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 主页数据类
 */
@Api(tags = "主页统计")
@Slf4j
@RestController
@RequestMapping("api/mainChart")
public class MainChartController extends CommonCtrl {

    @Autowired
    private PayOrderService payOrderService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private MchInfoService mchInfoService;


    /**
     * 周交易总金额
     */
    @ApiOperation("周交易总金额")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header")
    })
    @PreAuthorize("hasAuthority('ENT_MCH_MAIN_PAY_AMOUNT_WEEK')")
    @RequestMapping(value = "/payAmountWeek", method = RequestMethod.GET)
    public ApiRes payAmountWeek() {
        return ApiRes.ok(payOrderService.mainPageWeekCountBus(getCurrentMchNo()));
    }

    /**
     * 商户总数量、服务商总数量、总交易金额、总交易笔数
     *
     * @return
     */
    @ApiOperation("总交易金额、总交易笔数")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header")
    })
    @PreAuthorize("hasAuthority('ENT_MCH_MAIN_NUMBER_COUNT')")
    @RequestMapping(value = "/numCount", method = RequestMethod.GET)
    public ApiRes numCount() {
        return ApiRes.ok(payOrderService.mainPageNumCountBus(getCurrentMchNo()));
    }

    /**
     * 交易统计
     */
    @ApiOperation("交易统计")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "createdStart", value = "日期格式字符串（yyyy-MM-dd），时间范围查询--开始时间，须和结束时间一起使用，否则默认查最近七天（含今天）"),
            @ApiImplicitParam(name = "createdEnd", value = "日期格式字符串（yyyy-MM-dd），时间范围查询--结束时间，须和开始时间一起使用，否则默认查最近七天（含今天）")
    })
    @PreAuthorize("hasAuthority('ENT_MCH_MAIN_PAY_COUNT')")
    @RequestMapping(value = "/payCount", method = RequestMethod.GET)
    public ApiRes<List<Map>> payCount() {
        // 获取传入参数
        JSONObject paramJSON = getReqParamJSON();
        String createdStart = paramJSON.getString("createdStart");
        String createdEnd = paramJSON.getString("createdEnd");

        List<Map> mapList = payOrderService.mainPagePayCountBus(getCurrentMchNo(), createdStart, createdEnd);
        //返回数据
        return ApiRes.ok(mapList);
    }

    /**
     * 支付方式统计
     */
    @ApiOperation("支付方式统计")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "createdStart", value = "日期格式字符串（yyyy-MM-dd），时间范围查询--开始时间，须和结束时间一起使用，否则默认查最近七天（含今天）"),
            @ApiImplicitParam(name = "createdEnd", value = "日期格式字符串（yyyy-MM-dd），时间范围查询--结束时间，须和开始时间一起使用，否则默认查最近七天（含今天）")
    })
    @PreAuthorize("hasAuthority('ENT_MCH_MAIN_PAY_TYPE_COUNT')")
    @RequestMapping(value = "/payTypeCount", method = RequestMethod.GET)
    public ApiRes<ArrayList> payWayCount() {
        JSONObject paramJSON = getReqParamJSON();
        // 开始、结束时间
        String createdStart = paramJSON.getString("createdStart");
        String createdEnd = paramJSON.getString("createdEnd");
        ArrayList arrayResult = payOrderService.mainPagePayTypeCountBus(getCurrentMchNo(), createdStart, createdEnd);
        return ApiRes.ok(arrayResult);
    }

    /**
     * 商户基本信息、用户基本信息
     **/
    @ApiOperation("商户基本信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header")
    })
    @PreAuthorize("hasAuthority('ENT_MCH_MAIN_USER_INFO')")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ApiRes userDetail() {
        SysUser sysUser = sysUserService.getById(getCurrentUser().getSysUser().getSysUserId());
        MchInfo mchInfo = mchInfoService.getById(getCurrentMchNo());
        JSONObject json = (JSONObject) JSON.toJSON(mchInfo);
        json.put("loginUsername", sysUser.getLoginUsername());
        json.put("realname", sysUser.getRealname());
        return ApiRes.ok(json);
    }
}
