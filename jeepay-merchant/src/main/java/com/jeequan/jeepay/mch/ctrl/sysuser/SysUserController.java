package com.jeequan.jeepay.mch.ctrl.sysuser;

import cn.hutool.core.codec.Base64;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.SysUser;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiPageRes;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mch.ctrl.CommonCtrl;
import com.jeequan.jeepay.mch.service.AuthService;
import com.jeequan.jeepay.service.impl.SysUserAuthService;
import com.jeequan.jeepay.service.impl.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

/**
 * 用户管理类
 *
 * @author terrfly
 * @modify zhuxiao
 * @site https://www.jeequan.com
 * @date 2021-04-27 15:50
 */
@Api(tags = "系统管理（操作员）")
@RestController
@RequestMapping("api/sysUsers")
public class SysUserController extends CommonCtrl {

    @Autowired
    SysUserService sysUserService;
    @Autowired
    SysUserAuthService sysUserAuthService;
    @Autowired
    private AuthService authService;


    /**
     * list
     */
    @ApiOperation("操作员列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "pageNumber", value = "分页页码", dataType = "int", defaultValue = "1"),
            @ApiImplicitParam(name = "pageSize", value = "分页条数", dataType = "int", defaultValue = "20"),
            @ApiImplicitParam(name = "sysUserId", value = "用户ID"),
            @ApiImplicitParam(name = "realname", value = "用户姓名")
    })
    @PreAuthorize("hasAuthority( 'ENT_UR_USER_LIST' )")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ApiPageRes<SysUser> list() {

        SysUser queryObject = getObject(SysUser.class);

        LambdaQueryWrapper<SysUser> condition = SysUser.gw();
        condition.eq(SysUser::getSysType, CS.SYS_TYPE.MCH);
        condition.eq(SysUser::getBelongInfoId, getCurrentUser().getSysUser().getBelongInfoId());

        if (StringUtils.isNotEmpty(queryObject.getRealname())) {
            condition.like(SysUser::getRealname, queryObject.getRealname());
        }

        if (queryObject.getSysUserId() != null) {
            condition.eq(SysUser::getSysUserId, queryObject.getSysUserId());
        }

        IPage<SysUser> pages = sysUserService.page(getIPage(), condition);

        return ApiPageRes.pages(pages);
    }


    /**
     * detail
     */
    @ApiOperation("操作员详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "recordId", value = "用户ID", required = true, dataType = "Long")
    })
    @PreAuthorize("hasAuthority( 'ENT_UR_USER_EDIT' )")
    @RequestMapping(value = "/{recordId}", method = RequestMethod.GET)
    public ApiRes detail(@PathVariable("recordId") Integer recordId) {

        SysUser sysUser = sysUserService.getById(recordId);
        if (sysUser == null) {
            throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }
        if (!sysUser.getBelongInfoId().equals(getCurrentUser().getSysUser().getBelongInfoId())) {
            throw new BizException(ApiCodeEnum.SYS_PERMISSION_ERROR);
        }
        return ApiRes.ok(sysUser);
    }

    /**
     * add
     */
    @ApiOperation("添加操作员")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "isAdmin", value = "是否超管（超管拥有全部权限） 0-否 1-是", required = true, dataType = "Byte"),
            @ApiImplicitParam(name = "loginUsername", value = "登录用户名", required = true),
            @ApiImplicitParam(name = "realname", value = "真实姓名", required = true),
            @ApiImplicitParam(name = "sex", value = "性别 0-未知, 1-男, 2-女", required = true, dataType = "Byte"),
            @ApiImplicitParam(name = "telphone", value = "手机号", required = true),
            @ApiImplicitParam(name = "userNo", value = "员工编号", required = true),
            @ApiImplicitParam(name = "state", value = "状态: 0-停用, 1-启用", required = true, dataType = "Byte")
    })
    @PreAuthorize("hasAuthority( 'ENT_UR_USER_ADD' )")
    @RequestMapping(value = "", method = RequestMethod.POST)
    @MethodLog(remark = "添加操作员")
    public ApiRes add() {
        SysUser sysUser = getObject(SysUser.class);
        sysUser.setBelongInfoId(getCurrentUser().getSysUser().getBelongInfoId());
        sysUser.setIsAdmin(CS.NO);
        sysUserService.addSysUser(sysUser, CS.SYS_TYPE.MCH);
        return ApiRes.ok();
    }


    /**
     * 修改操作员 登录认证信息
     */
//	@RequestMapping(value="/modifyPwd", method = RequestMethod.PUT)
    public ApiRes authInfo() {

        Long opSysUserId = getValLongRequired("recordId");   //操作员ID

        //更改密码， 验证当前用户信息
        String currentUserPwd = getValStringRequired("originalPwd"); //当前用户登录密码
        //验证当前密码是否正确
        if (!sysUserAuthService.validateCurrentUserPwd(currentUserPwd)) {
            throw new BizException("原密码验证失败！");
        }

        String opUserPwd = getValStringRequired("confirmPwd");

        // 验证原密码与新密码是否相同
        if (opUserPwd.equals(currentUserPwd)) {
            throw new BizException("新密码与原密码相同！");
        }

        sysUserAuthService.resetAuthInfo(opSysUserId, null, null, opUserPwd, CS.SYS_TYPE.MCH);
        return ApiRes.ok();
    }


    /**
     * update
     */
    @ApiOperation("修改操作员信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "recordId", value = "用户ID", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "isAdmin", value = "是否超管（超管拥有全部权限） 0-否 1-是", required = true, dataType = "Byte"),
            @ApiImplicitParam(name = "loginUsername", value = "登录用户名", required = true),
            @ApiImplicitParam(name = "realname", value = "真实姓名", required = true),
            @ApiImplicitParam(name = "sex", value = "性别 0-未知, 1-男, 2-女", required = true, dataType = "Byte"),
            @ApiImplicitParam(name = "telphone", value = "手机号", required = true),
            @ApiImplicitParam(name = "userNo", value = "员工编号", required = true),
            @ApiImplicitParam(name = "state", value = "状态: 0-停用, 1-启用", required = true, dataType = "Byte"),
            @ApiImplicitParam(name = "resetPass", value = "是否重置密码", dataType = "Boolean"),
            @ApiImplicitParam(name = "confirmPwd", value = "待更新的密码，base64加密"),
            @ApiImplicitParam(name = "defaultPass", value = "是否默认密码", dataType = "Boolean")
    })
    @PreAuthorize("hasAuthority( 'ENT_UR_USER_EDIT' )")
    @RequestMapping(value = "/{recordId}", method = RequestMethod.PUT)
    @MethodLog(remark = "修改操作员信息")
    public ApiRes update(@PathVariable("recordId") Long recordId) {
        SysUser sysUser = getObject(SysUser.class);
        sysUser.setSysUserId(recordId);
        // 如果当前用户为非超管则用户状态为普通用户
        if (getCurrentUser().getSysUser().getIsAdmin() != CS.YES) {
            sysUser.setIsAdmin(CS.NO);
        }
        SysUser dbRecord = sysUserService.getOne(SysUser.gw().eq(SysUser::getSysUserId, recordId).eq(SysUser::getBelongInfoId, getCurrentMchNo()));
        if (dbRecord == null) {
            throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }

        //判断是否自己禁用自己
        if (recordId.equals(getCurrentUser().getSysUser().getSysUserId()) && sysUser.getState() != null && sysUser.getState() == CS.PUB_DISABLE) {
            throw new BizException("系统不允许禁用当前登陆用户！");
        }

        //判断是否重置密码
        Boolean resetPass = getReqParamJSON().getBoolean("resetPass");
        if (resetPass != null && resetPass) {
            //判断是否重置密码
            String updatePwd = getReqParamJSON().getBoolean("defaultPass") == false ? Base64.decodeStr(getValStringRequired("confirmPwd")) : CS.DEFAULT_PWD;
            sysUserAuthService.resetAuthInfo(sysUser.getSysUserId(), null, null, updatePwd, CS.SYS_TYPE.MCH);
            // 删除用户redis缓存信息
            authService.delAuthentication(Arrays.asList(recordId));
        }

        sysUserService.updateSysUser(sysUser);

        //如果用户被禁用，需要更新redis数据
        if (sysUser.getState() != null && sysUser.getState() == CS.PUB_DISABLE) {
            authService.refAuthentication(Arrays.asList(recordId));
        }

        return ApiRes.ok();
    }

    /**
     * delete
     */
    @ApiOperation("删除操作员信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "recordId", value = "用户ID", required = true, dataType = "Long")
    })
    @PreAuthorize("hasAuthority( 'ENT_UR_USER_DELETE' )")
    @RequestMapping(value = "/{recordId}", method = RequestMethod.DELETE)
    @MethodLog(remark = "删除操作员信息")
    public ApiRes delete(@PathVariable("recordId") Long recordId) {
        //查询该操作员信息
        SysUser sysUser = sysUserService.getById(recordId);
        if (sysUser == null) {
            throw new BizException("该操作员不存在！");
        }

        //判断是否自己删除自己
        if (recordId.equals(getCurrentUser().getSysUser().getSysUserId())) {
            throw new BizException("系统不允许删除当前登陆用户！");
        }

        //判断是否删除商户默认超管
        SysUser mchUserDefault = sysUserService.getOne(SysUser.gw()
                .eq(SysUser::getBelongInfoId, getCurrentMchNo())
                .eq(SysUser::getSysType, CS.SYS_TYPE.MCH)
                .eq(SysUser::getIsAdmin, CS.YES)
        );

        if (mchUserDefault.getSysUserId().equals(recordId)) {
            throw new BizException("系统不允许删除商户默认用户！");
        }

        // 删除用户
        sysUserService.removeUser(sysUser, CS.SYS_TYPE.MCH);

        //如果用户被删除，需要更新redis数据
        authService.refAuthentication(Arrays.asList(recordId));

        return ApiRes.ok();
    }
}
