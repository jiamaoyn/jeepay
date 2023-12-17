package com.jeequan.jeepay.mch.ctrl.merchant;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.domain.AlipayDataBillAccountlogQueryModel;
import com.alipay.api.request.AlipayDataBillAccountlogQueryRequest;
import com.alipay.api.response.AlipayDataBillAccountlogQueryResponse;
import com.jeequan.jeepay.components.mq.model.ResetIsvMchAppInfoConfigMQ;
import com.jeequan.jeepay.components.mq.vender.IMQSender;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchApp;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.entity.PayInterfaceConfig;
import com.jeequan.jeepay.core.entity.PayInterfaceDefine;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.model.DBApplicationConfig;
import com.jeequan.jeepay.core.model.params.NormalMchParams;
import com.jeequan.jeepay.core.model.params.alipay.AlipayNormalMchParams;
import com.jeequan.jeepay.mch.ctrl.CommonCtrl;
import com.jeequan.jeepay.mch.model.AlipayClientWrapper;
import com.jeequan.jeepay.service.impl.MchAppService;
import com.jeequan.jeepay.service.impl.MchInfoService;
import com.jeequan.jeepay.service.impl.PayInterfaceConfigService;
import com.jeequan.jeepay.service.impl.SysConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 商户支付接口配置类
 *
 * @author zhuxiao
 * @site https://www.jeequan.com
 * @date 2021-04-27 15:50
 */
@Api(tags = "商户支付接口管理")
@RestController
@RequestMapping("/api/mch/payConfigs")
public class MchPayInterfaceConfigController extends CommonCtrl {

    @Autowired
    private PayInterfaceConfigService payInterfaceConfigService;
    @Autowired
    private MchInfoService mchInfoService;
    @Autowired
    private MchAppService mchAppService;
    @Autowired
    private SysConfigService sysConfigService;
    @Autowired
    private IMQSender mqSender;

    /**
     * 查询商户支付接口配置列表
     */
    @ApiOperation("查询应用支付接口配置列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "appId", value = "应用ID", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_MCH_PAY_CONFIG_LIST')")
    @GetMapping
    public ApiRes<List<PayInterfaceDefine>> list() {
        MchInfo mchInfo = mchInfoService.getById(getCurrentUser().getSysUser().getBelongInfoId());
        List<PayInterfaceDefine> list = payInterfaceConfigService.selectAllPayIfConfigListByAppId(getValStringRequired("appId"));

        for (PayInterfaceDefine define : list) {
            define.addExt("mchParams", mchInfo.getType() == CS.MCH_TYPE_NORMAL ? define.getNormalMchParams() : define.getIsvsubMchParams());
            define.setNormalMchParams(null);
            define.setIsvsubMchParams(null);
        }
        return ApiRes.ok(list);
    }

    /**
     * 根据 商户号、接口类型 获取商户参数配置
     */
    @ApiOperation("根据应用ID、接口类型 获取应用参数配置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "appId", value = "应用ID", required = true),
            @ApiImplicitParam(name = "ifCode", value = "接口类型代码", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_MCH_PAY_CONFIG_VIEW')")
    @GetMapping("/{appId}/{ifCode}")
    public ApiRes getByMchNo(@PathVariable(value = "appId") String appId, @PathVariable(value = "ifCode") String ifCode) {
        PayInterfaceConfig payInterfaceConfig = payInterfaceConfigService.getByInfoIdAndIfCode(CS.INFO_TYPE_MCH_APP, appId, ifCode);
        if (payInterfaceConfig != null) {
            // 费率转换为百分比数值
            if (payInterfaceConfig.getIfRate() != null) {
                payInterfaceConfig.setIfRate(payInterfaceConfig.getIfRate().multiply(new BigDecimal("100")));
            }

            // 敏感数据脱敏
            if (StringUtils.isNotBlank(payInterfaceConfig.getIfParams())) {
                MchInfo mchInfo = mchInfoService.getById(getCurrentMchNo());

                // 普通商户的支付参数执行数据脱敏
                if (mchInfo.getType() == CS.MCH_TYPE_NORMAL) {
                    NormalMchParams mchParams = NormalMchParams.factory(payInterfaceConfig.getIfCode(), payInterfaceConfig.getIfParams());
                    if (mchParams != null) {
                        payInterfaceConfig.setIfParams(mchParams.deSenData());
                    }
                }
            }
        }
        return ApiRes.ok(payInterfaceConfig);
    }

    /**
     * 更新商户支付参数
     */
    @ApiOperation("更新商户支付参数")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "infoId", value = "应用AppId", required = true),
            @ApiImplicitParam(name = "ifCode", value = "接口类型代码", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_MCH_PAY_CONFIG_ADD')")
    @PostMapping
    @MethodLog(remark = "更新商户支付参数")
    public ApiRes saveOrUpdate() {

        String ifCode = getValStringRequired("ifCode");
        String infoId = getValStringRequired("infoId");
        MchApp mchApp = mchAppService.getById(infoId);
        if (mchApp == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }
        PayInterfaceConfig payInterfaceConfig = getObject(PayInterfaceConfig.class);
        payInterfaceConfig.setInfoType(CS.INFO_TYPE_MCH_APP);
        payInterfaceConfig.setInfoId(infoId);

        // 存入真实费率
        if (payInterfaceConfig.getIfRate() != null) {
            payInterfaceConfig.setIfRate(payInterfaceConfig.getIfRate().divide(new BigDecimal("100"), 6, BigDecimal.ROUND_HALF_UP));
        }

        //添加更新者信息
        Long userId = getCurrentUser().getSysUser().getSysUserId();
        String realName = getCurrentUser().getSysUser().getRealname();
        payInterfaceConfig.setUpdatedUid(userId);
        payInterfaceConfig.setUpdatedBy(realName);

        //根据 商户号、接口类型 获取商户参数配置
        PayInterfaceConfig dbRecoed = payInterfaceConfigService.getByInfoIdAndIfCode(CS.INFO_TYPE_MCH_APP, infoId, ifCode);
        //若配置存在，为saveOrUpdate添加ID，第一次配置添加创建者
        if (dbRecoed != null) {
            mqSender.send(ResetIsvMchAppInfoConfigMQ.build(ResetIsvMchAppInfoConfigMQ.RESET_TYPE_TELEGRAM_APP, null, getCurrentMchNo(), infoId,"正在修改系统应用--已被禁止\n系统应用："+mchApp.getAppName()));
            return ApiRes.fail(ApiCodeEnum.SYS_PROHIBIT_ERROR);
        } else {
            payInterfaceConfig.setCreatedUid(userId);
            payInterfaceConfig.setCreatedBy(realName);
        }
        if (ifCode.equals(CS.IF_CODE.ALIPAY)){
            MchInfo mchInfo = mchInfoService.getById(mchApp.getMchNo());
            if (mchInfo != null && mchInfo.getType() == MchInfo.TYPE_NORMAL){
                AlipayNormalMchParams alipayParams = (AlipayNormalMchParams) NormalMchParams.factory(ifCode, payInterfaceConfig.getIfParams());
                if (alipayParams==null)return ApiRes.customFail("系统错误");
                AlipayClientWrapper alipayClientWrapper = AlipayClientWrapper.buildAlipayClientWrapper(alipayParams);
                AlipayDataBillAccountlogQueryRequest request = new AlipayDataBillAccountlogQueryRequest();
                AlipayDataBillAccountlogQueryModel model = new AlipayDataBillAccountlogQueryModel();
                Date startDate = DateUtil.offsetMinute(new Date(), -0);
                Date endDate = DateUtil.offsetMinute(new Date(), -1);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                model.setStartTime(sdf.format(endDate));
                model.setEndTime(sdf.format(startDate));
                request.setBizModel(model);
                AlipayDataBillAccountlogQueryResponse resp = alipayClientWrapper.execute(request);
                if(!resp.isSuccess()){
                    mqSender.send(ResetIsvMchAppInfoConfigMQ.build(ResetIsvMchAppInfoConfigMQ.RESET_TYPE_TELEGRAM_APP, null, getCurrentMchNo(), infoId,"创建支付参数--密钥配置错误\n系统应用："+mchApp.getAppName()));
                    return ApiRes.customFail(resp.getMsg()+resp.getSubMsg());
                }
            }
        }
        boolean result = payInterfaceConfigService.saveOrUpdate(payInterfaceConfig);
        if (!result) {
            mqSender.send(ResetIsvMchAppInfoConfigMQ.build(ResetIsvMchAppInfoConfigMQ.RESET_TYPE_TELEGRAM_APP, null, getCurrentMchNo(), infoId,"创建支付参数--系统错误创建失败\n系统应用："+mchApp.getAppName()));
            throw new BizException("配置失败");
        }
        mqSender.send(ResetIsvMchAppInfoConfigMQ.build(ResetIsvMchAppInfoConfigMQ.RESET_TYPE_TELEGRAM_APP, null, getCurrentMchNo(), infoId,"创建支付参数--创建成功\n系统应用："+mchApp.getAppName()));
        mqSender.send(ResetIsvMchAppInfoConfigMQ.build(ResetIsvMchAppInfoConfigMQ.RESET_TYPE_MCH_APP, null, getCurrentMchNo(), infoId));

        return ApiRes.ok();
    }

    /**
     * 查询支付宝商户授权URL
     **/
    @ApiOperation("查询支付宝商户授权URL")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "mchAppId", value = "应用ID", required = true)
    })
    @GetMapping("/alipayIsvsubMchAuthUrls/{mchAppId}")
    public ApiRes queryAlipayIsvsubMchAuthUrl(@PathVariable String mchAppId) {

        MchApp mchApp = mchAppService.getById(mchAppId);

        if (mchApp == null || !mchApp.getMchNo().equals(getCurrentMchNo())) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }

        MchInfo mchInfo = mchInfoService.getById(mchApp.getMchNo());
        DBApplicationConfig dbApplicationConfig = sysConfigService.getDBApplicationConfig();
        String authUrl = dbApplicationConfig.genAlipayIsvsubMchAuthUrl(mchInfo.getIsvNo(), mchAppId);
        String authQrImgUrl = dbApplicationConfig.genScanImgUrl(authUrl);

        JSONObject result = new JSONObject();
        result.put("authUrl", authUrl);
        result.put("authQrImgUrl", authQrImgUrl);
        return ApiRes.ok(result);
    }


    /**
     * 查询当前应用支持的支付接口
     */
    @ApiOperation("查询当前应用支持的支付接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "appId", value = "应用ID", required = true)
    })
    @PreAuthorize("hasAuthority( 'ENT_DIVISION_RECEIVER_ADD' )")
    @RequestMapping(value = "ifCodes/{appId}", method = RequestMethod.GET)
    public ApiRes<Set<String>> getIfCodeByAppId(@PathVariable("appId") String appId) {

        if (mchAppService.count(MchApp.gw().eq(MchApp::getMchNo, getCurrentMchNo()).eq(MchApp::getAppId, appId)) <= 0) {
            throw new BizException("商户应用不存在");
        }

        Set<String> result = new HashSet<>();

        payInterfaceConfigService.list(PayInterfaceConfig.gw().select(PayInterfaceConfig::getIfCode)
                .eq(PayInterfaceConfig::getState, CS.PUB_USABLE)
                .eq(PayInterfaceConfig::getInfoId, appId)
                .eq(PayInterfaceConfig::getInfoType, CS.INFO_TYPE_MCH_APP)
        ).stream().forEach(r -> result.add(r.getIfCode()));

        return ApiRes.ok(result);
    }


}
