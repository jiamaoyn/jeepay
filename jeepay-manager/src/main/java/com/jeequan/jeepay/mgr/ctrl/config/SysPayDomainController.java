package com.jeequan.jeepay.mgr.ctrl.config;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.entity.SysPayDomain;
import com.jeequan.jeepay.core.model.ApiPageRes;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.SysPayDomainService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "系统管理（支付域名）")
@Slf4j
@RestController
@RequestMapping("api/sysPayDomain")
public class SysPayDomainController extends CommonCtrl {
    @Autowired
    private SysPayDomainService sysPayDomainService;

    /**
     * 商户信息列表
     */
    @ApiOperation("查询支付域名")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "pageNumber", value = "分页页码", dataType = "int", defaultValue = "1"),
            @ApiImplicitParam(name = "pageSize", value = "分页条数", dataType = "int", defaultValue = "20"),
            @ApiImplicitParam(name = "ip", value = "ip"),
            @ApiImplicitParam(name = "domain", value = "domain"),
            @ApiImplicitParam(name = "state", value = "状态: 0-停用, 1-启用", dataType = "Byte"),
    })
    @PreAuthorize("hasAuthority('ENT_SYS_PAY_DOMAIN_LIST')")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ApiPageRes<SysPayDomain> list() {
        SysPayDomain sysPayDomain = getObject(SysPayDomain.class);
        IPage<SysPayDomain> pages = sysPayDomainService.selectPage(getIPage(), sysPayDomain);
        return ApiPageRes.pages(pages);
    }

    @ApiOperation("新增支付域名")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "ip", value = "ip"),
            @ApiImplicitParam(name = "domain", value = "domain"),
            @ApiImplicitParam(name = "state", value = "状态: 0-停用, 1-启用", dataType = "Byte")
    })
    @PreAuthorize("hasAuthority('ENT_SYS_PAY_DOMAIN_ADD')")
    @MethodLog(remark = "新增支付域名")
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ApiRes add() {
        SysPayDomain sysPayDomain = getObject(SysPayDomain.class);
        sysPayDomainService.addPayDomain(sysPayDomain);
        return ApiRes.ok();
    }

    @ApiOperation("删除支付域名")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "id", value = "id", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_SYS_PAY_DOMAIN_DELETE')")
    @MethodLog(remark = "删除支付域名")
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ApiRes delete(@PathVariable("id") String id) {
        sysPayDomainService.removeByIdKey(id);
        return ApiRes.ok();
    }

    @ApiOperation("更新支付域名")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "ip", value = "ip"),
            @ApiImplicitParam(name = "domain", value = "domain"),
            @ApiImplicitParam(name = "state", value = "状态: 0-停用, 1-启用", dataType = "Byte")
    })
    @PreAuthorize("hasAuthority('ENT_SYS_PAY_DOMAIN_EDIT')")
    @MethodLog(remark = "更新支付域名")
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ApiRes update(@PathVariable("id") String id) {

        //获取查询条件
        SysPayDomain sysPayDomain = getObject(SysPayDomain.class);
        sysPayDomain.setId(Integer.valueOf(id)); //设置主键
        //更新商户信息
        if (!sysPayDomainService.updateById(sysPayDomain)) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_UPDATE);
        }

        return ApiRes.ok();
    }
    @ApiOperation("查询支付域名")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "mchNo", value = "id", required = true)
    })
    @PreAuthorize("hasAnyAuthority('ENT_SYS_PAY_DOMAIN_VIEW', 'ENT_SYS_PAY_DOMAIN_EDIT')")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ApiRes detail(@PathVariable("id") String id) {
        SysPayDomain sysPayDomain = sysPayDomainService.selectById(id);
        if (sysPayDomain == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }
        return ApiRes.ok(sysPayDomain);
    }

}
