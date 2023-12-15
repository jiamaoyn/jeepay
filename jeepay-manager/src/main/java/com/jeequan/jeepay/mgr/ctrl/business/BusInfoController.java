package com.jeequan.jeepay.mgr.ctrl.business;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.components.mq.model.CleanMchLoginAuthCacheMQ;
import com.jeequan.jeepay.components.mq.model.ResetIsvMchAppInfoConfigMQ;
import com.jeequan.jeepay.components.mq.vender.IMQSender;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.entity.SysUser;
import com.jeequan.jeepay.core.model.ApiPageRes;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.MchInfoService;
import com.jeequan.jeepay.service.impl.SysUserAuthService;
import com.jeequan.jeepay.service.impl.SysUserService;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 商户管理类
 *
 * @date 2021-06-07 07:15
 */
@Api(tags = "商户基本信息管理")
@RestController
@RequestMapping("/api/busInfo")
public class BusInfoController extends CommonCtrl {

    @Autowired
    private MchInfoService mchInfoService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysUserAuthService sysUserAuthService;
    @Autowired
    private IMQSender mqSender;

    /**
     * @describe: 商户信息列表
     */
    @ApiOperation("查询商家列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "pageNumber", value = "分页页码", dataType = "int", defaultValue = "1"),
            @ApiImplicitParam(name = "pageSize", value = "分页条数", dataType = "int", defaultValue = "20"),
            @ApiImplicitParam(name = "mchNo", value = "商户号"),
            @ApiImplicitParam(name = "mchName", value = "商户名称"),
            @ApiImplicitParam(name = "mchNoPid", value = "商户上级商户号"),
            @ApiImplicitParam(name = "isvNo", value = "服务商号"),
            @ApiImplicitParam(name = "state", value = "状态: 0-停用, 1-启用", dataType = "Byte")
    })
    @PreAuthorize("hasAuthority('ENT_MCH_LIST')")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ApiPageRes<MchInfo> list() {
        MchInfo mchInfo = getObject(MchInfo.class);
        mchInfo.setType((byte) 3);
        IPage<MchInfo> pages = mchInfoService.selectPage(getIPage(), mchInfo);
        return ApiPageRes.pages(pages);
    }

    /**
     * @describe: 新增商家信息
     */
    @ApiOperation("新增商户信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "mchName", value = "商户名称", required = true),
            @ApiImplicitParam(name = "mchShortName", value = "商户简称", required = true),
            @ApiImplicitParam(name = "loginUserName", value = "登录名", required = true),
            @ApiImplicitParam(name = "isvNo", value = "服务商号，type为2时必填"),
            @ApiImplicitParam(name = "contactName", value = "联系人姓名", required = true),
            @ApiImplicitParam(name = "contactTel", value = "联系人手机号", required = true),
            @ApiImplicitParam(name = "contactEmail", value = "联系人邮箱"),
            @ApiImplicitParam(name = "remark", value = "备注"),
            @ApiImplicitParam(name = "state", value = "状态: 0-停用, 1-启用", dataType = "Byte")
    })
    @PreAuthorize("hasAuthority('ENT_MCH_INFO_ADD')")
    @MethodLog(remark = "新增商户")
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ApiRes add() {
        MchInfo mchInfo = getObject(MchInfo.class);
        mchInfo.setType((byte) 3);
        // 获取传入的商户登录名
        String loginUserName = getValStringRequired("loginUserName");
        mchInfo.setMchNo("B" + DateUtil.currentSeconds());
        // 当前登录用户信息
        SysUser sysUser = getCurrentUser().getSysUser();
        mchInfo.setCreatedUid(sysUser.getSysUserId());
        mchInfo.setCreatedBy(sysUser.getRealname());

        mchInfoService.addMch(mchInfo, loginUserName);
        return ApiRes.ok();
    }

    /**
     * @describe: 删除商户信息
     */
    @ApiOperation("删除商户信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "mchNo", value = "商户号", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_MCH_INFO_DEL')")
    @MethodLog(remark = "删除商户")
    @RequestMapping(value = "/{mchNo}", method = RequestMethod.DELETE)
    public ApiRes delete(@PathVariable("mchNo") String mchNo) {
        List<Long> userIdList = mchInfoService.removeByMchNo(mchNo);

        // 推送mq删除redis用户缓存
        mqSender.send(CleanMchLoginAuthCacheMQ.build(userIdList));

        // 推送mq到目前节点进行更新数据
        mqSender.send(ResetIsvMchAppInfoConfigMQ.build(ResetIsvMchAppInfoConfigMQ.RESET_TYPE_MCH_INFO, null, mchNo, null));
        return ApiRes.ok();
    }

    /**
     * @describe: 更新商户信息
     */
    @ApiOperation("更新商户信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "mchName", value = "商户名称", required = true),
            @ApiImplicitParam(name = "mchShortName", value = "商户简称", required = true),
            @ApiImplicitParam(name = "loginUserName", value = "登录名", required = true),
            @ApiImplicitParam(name = "contactName", value = "联系人姓名", required = true),
            @ApiImplicitParam(name = "contactTel", value = "联系人手机号", required = true),
            @ApiImplicitParam(name = "contactEmail", value = "联系人邮箱"),
            @ApiImplicitParam(name = "remark", value = "备注"),
            @ApiImplicitParam(name = "state", value = "状态: 0-停用, 1-启用", dataType = "Byte"),
            @ApiImplicitParam(name = "resetPass", value = "是否重置密码", dataType = "Boolean"),
            @ApiImplicitParam(name = "confirmPwd", value = "待更新的密码，base64加密"),
            @ApiImplicitParam(name = "defaultPass", value = "是否默认密码", dataType = "Boolean")
    })
    @PreAuthorize("hasAuthority('ENT_MCH_INFO_EDIT')")
    @MethodLog(remark = "更新商户信息")
    @RequestMapping(value = "/{mchNo}", method = RequestMethod.PUT)
    public ApiRes update(@PathVariable("mchNo") String mchNo) {

        //获取查询条件
        MchInfo mchInfo = getObject(MchInfo.class);
        mchInfo.setMchNo(mchNo); //设置商户号主键

        mchInfo.setType(null); //防止变更商户类型
        mchInfo.setIsvNo(null);

        // 待删除用户登录信息的ID list
        Set<Long> removeCacheUserIdList = new HashSet<>();

        // 如果商户状态为禁用状态，清除该商户用户登录信息
        if (mchInfo.getState() == CS.NO) {
            sysUserService.list(SysUser.gw().select(SysUser::getSysUserId).eq(SysUser::getBelongInfoId, mchNo).eq(SysUser::getSysType, CS.SYS_TYPE.MCH))
                    .stream().forEach(u -> removeCacheUserIdList.add(u.getSysUserId()));
        }

        //判断是否重置密码
        if (getReqParamJSON().getBooleanValue("resetPass")) {
            // 待更新的密码
            String updatePwd = getReqParamJSON().getBoolean("defaultPass") ? CS.DEFAULT_PWD : Base64.decodeStr(getValStringRequired("confirmPwd"));
            // 获取商户超管
            Long mchAdminUserId = sysUserService.findMchAdminUserId(mchNo);

            //重置超管密码
            sysUserAuthService.resetAuthInfo(mchAdminUserId, null, null, updatePwd, CS.SYS_TYPE.MCH);

            //删除超管登录信息
            removeCacheUserIdList.add(mchAdminUserId);
        }

        // 推送mq删除redis用户认证信息
        if (!removeCacheUserIdList.isEmpty()) {
            mqSender.send(CleanMchLoginAuthCacheMQ.build(new ArrayList<>(removeCacheUserIdList)));
        }

        //更新商户信息
        if (!mchInfoService.updateById(mchInfo)) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_UPDATE);
        }

        // 推送mq到目前节点进行更新数据
        mqSender.send(ResetIsvMchAppInfoConfigMQ.build(ResetIsvMchAppInfoConfigMQ.RESET_TYPE_MCH_INFO, null, mchNo, null));

        return ApiRes.ok();
    }

    /**
     * @describe: 查询商户信息
     */
    @ApiOperation("查询商户信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "mchNo", value = "商户号", required = true)
    })
    @PreAuthorize("hasAnyAuthority('ENT_MCH_INFO_VIEW', 'ENT_MCH_INFO_EDIT')")
    @RequestMapping(value = "/{mchNo}", method = RequestMethod.GET)
    public ApiRes detail(@PathVariable("mchNo") String mchNo) {
        MchInfo mchInfo = mchInfoService.selectById(mchNo);
        if (mchInfo == null || mchInfo.getType() != (byte)3) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }

        SysUser sysUser = sysUserService.getById(mchInfo.getInitUserId());
        if (sysUser != null) {
            mchInfo.addExt("loginUserName", sysUser.getLoginUsername());
        }
        return ApiRes.ok(mchInfo);
    }
}
