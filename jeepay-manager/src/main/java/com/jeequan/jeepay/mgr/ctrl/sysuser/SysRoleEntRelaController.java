package com.jeequan.jeepay.mgr.ctrl.sysuser;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.entity.SysRoleEntRela;
import com.jeequan.jeepay.core.model.ApiPageRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.SysRoleEntRelaService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/*
 * 角色 权限管理
 * @date 2021/6/8 17:13
 */
@Api(tags = "系统管理（用户-角色-权限关联信息）")
@RestController
@RequestMapping("api/sysRoleEntRelas")
public class SysRoleEntRelaController extends CommonCtrl {

    @Autowired
    private SysRoleEntRelaService sysRoleEntRelaService;

    /**
     * list
     */
    @ApiOperation("关联关系--角色-权限关联信息列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "pageNumber", value = "分页页码", dataType = "int", defaultValue = "1"),
            @ApiImplicitParam(name = "pageSize", value = "分页条数（-1时查全部数据）", dataType = "int", defaultValue = "20"),
            @ApiImplicitParam(name = "roleId", value = "角色ID, ROLE_开头")
    })
    @PreAuthorize("hasAnyAuthority( 'ENT_UR_ROLE_ADD', 'ENT_UR_ROLE_DIST' )")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ApiPageRes<SysRoleEntRela> list() {

        SysRoleEntRela queryObject = getObject(SysRoleEntRela.class);

        LambdaQueryWrapper<SysRoleEntRela> condition = SysRoleEntRela.gw();

        if (queryObject.getRoleId() != null) {
            condition.eq(SysRoleEntRela::getRoleId, queryObject.getRoleId());
        }

        IPage<SysRoleEntRela> pages = sysRoleEntRelaService.page(getIPage(true), condition);

        return ApiPageRes.pages(pages);
    }

}
