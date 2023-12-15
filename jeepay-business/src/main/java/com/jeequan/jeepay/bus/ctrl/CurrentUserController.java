package com.jeequan.jeepay.bus.ctrl;

import cn.hutool.core.codec.Base64;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.components.mq.model.ResetIsvMchAppInfoConfigMQ;
import com.jeequan.jeepay.components.mq.vender.IMQSender;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.cache.ITokenService;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.entity.SysEntitlement;
import com.jeequan.jeepay.core.entity.SysUser;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.model.security.JeeUserDetails;
import com.jeequan.jeepay.core.utils.StringKit;
import com.jeequan.jeepay.core.utils.TreeDataBuilder;
import com.jeequan.jeepay.service.impl.MchInfoService;
import com.jeequan.jeepay.service.impl.SysEntitlementService;
import com.jeequan.jeepay.service.impl.SysUserAuthService;
import com.jeequan.jeepay.service.impl.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 当前登录者的信息相关接口
 */
@Api(tags = "登录者信息")
@RestController
@RequestMapping("api/current")
public class CurrentUserController extends CommonCtrl {

    @Autowired
    private SysEntitlementService sysEntitlementService;
    @Autowired
    private MchInfoService mchInfoService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysUserAuthService sysUserAuthService;
    @Autowired
    private IMQSender mqSender;

    @ApiOperation("查询当前登录者的用户信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header")
    })
    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public ApiRes currentUserInfo() {

        ///当前用户信息
        JeeUserDetails jeeUserDetails = getCurrentUser();
        SysUser user = jeeUserDetails.getSysUser();

        //1. 当前用户所有权限ID集合
        List<String> entIdList = new ArrayList<>();
        jeeUserDetails.getAuthorities().stream().forEach(r -> entIdList.add(r.getAuthority()));

        List<SysEntitlement> allMenuList = new ArrayList<>();    //所有菜单集合

        //2. 查询出用户所有菜单集合 (包含左侧显示菜单 和 其他类型菜单 )
        if (!entIdList.isEmpty()) {
            allMenuList = sysEntitlementService.list(SysEntitlement.gw()
                    .in(SysEntitlement::getEntId, entIdList)
                    .in(SysEntitlement::getEntType, Arrays.asList(CS.ENT_TYPE.MENU_LEFT, CS.ENT_TYPE.MENU_OTHER))
                    .eq(SysEntitlement::getSysType, CS.SYS_TYPE.MCH)
                    .eq(SysEntitlement::getState, CS.PUB_USABLE));
        }

        //4. 转换为json树状结构
        JSONArray jsonArray = (JSONArray) JSON.toJSON(allMenuList);
        List<JSONObject> allMenuRouteTree = new TreeDataBuilder(jsonArray,
                "entId", "pid", "children", "entSort", true)
                .buildTreeObject();

        //1. 所有权限ID集合
        user.addExt("entIdList", entIdList);
        user.addExt("allMenuRouteTree", allMenuRouteTree);
        MchInfo mchInfo = mchInfoService.selectById(user.getBelongInfoId());
        if (mchInfo == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }
        user.addExt("secret",StringKit.str2Star(mchInfo.getSecret(), 6, 6, 6));
        return ApiRes.ok(getCurrentUser().getSysUser());
    }


    /**
     * 修改个人信息
     */
    @ApiOperation("修改个人信息--基本信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "avatarUrl", value = "头像地址"),
            @ApiImplicitParam(name = "realname", value = "真实姓名"),
            @ApiImplicitParam(name = "sex", value = "性别 0-未知, 1-男, 2-女")
    })
    @MethodLog(remark = "修改个人信息")
    @RequestMapping(value = "/user", method = RequestMethod.PUT)
    public ApiRes modifyCurrentUserInfo() {

        //修改头像
        String avatarUrl = getValString("avatarUrl");
        String realname = getValString("realname");
        Byte sex = getValByte("sex");
        SysUser updateRecord = new SysUser();
        updateRecord.setSysUserId(getCurrentUser().getSysUser().getSysUserId());
        if (StringUtils.isNotEmpty(avatarUrl)) {
            updateRecord.setAvatarUrl(avatarUrl);
        }
        if (StringUtils.isNotEmpty(realname)) {
            updateRecord.setRealname(realname);
        }
        if (sex != null) {
            updateRecord.setSex(sex);
        }
        sysUserService.updateById(updateRecord);


        //保存redis最新数据
        JeeUserDetails currentUser = getCurrentUser();
        currentUser.setSysUser(sysUserService.getById(getCurrentUser().getSysUser().getSysUserId()));
        ITokenService.refData(currentUser);

        return ApiRes.ok();
    }


    /**
     * modifyPwd
     */
    @ApiOperation("修改个人信息--安全信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "confirmPwd", value = "新密码"),
            @ApiImplicitParam(name = "originalPwd", value = "原密码")
    })
    @MethodLog(remark = "修改密码")
    @RequestMapping(value = "modifyPwd", method = RequestMethod.PUT)
    public ApiRes modifyPwd() throws BizException {

        Long opSysUserId = getValLongRequired("recordId");   //操作员ID

        //更改密码，验证当前用户信息
        String currentUserPwd = Base64.decodeStr(getValStringRequired("originalPwd")); //当前用户登录密码
        //验证当前密码是否正确
        if (!sysUserAuthService.validateCurrentUserPwd(currentUserPwd)) {
            throw new BizException("原密码验证失败！");
        }

        String opUserPwd = Base64.decodeStr(getValStringRequired("confirmPwd"));

        // 验证原密码与新密码是否相同
        if (opUserPwd.equals(currentUserPwd)) {
            throw new BizException("新密码与原密码不能相同！");
        }

        sysUserAuthService.resetAuthInfo(opSysUserId, null, null, opUserPwd, CS.SYS_TYPE.MCH);
        //调用登出接口
        return logout();
    }

    /**
     * 修改商家密钥
     */
    @ApiOperation("修改商家密钥")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "secret", value = "商家密钥---新")
    })
    @MethodLog(remark = "修改商家密钥")
    @RequestMapping(value = "/userSecret", method = RequestMethod.PUT)
    public ApiRes modifyCurrentUserInfoSecret() {

        //修改头像
        String secret = getValString("secret");
        String mchNo = getCurrentUser().getSysUser().getBelongInfoId();
        MchInfo mchInfo = new MchInfo();
        mchInfo.setMchNo(mchNo);
        if (StringUtils.isNotEmpty(secret)) {
            mchInfo.setSecret(secret);
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
     * 登出
     */
    @ApiOperation("退出登录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header")
    })
    @MethodLog(remark = "退出")
    @RequestMapping(value = "logout", method = RequestMethod.POST)
    public ApiRes logout() throws BizException {

        ITokenService.removeIToken(getCurrentUser().getCacheKey(), getCurrentUser().getSysUser().getSysUserId());
        return ApiRes.ok();
    }


}
