package com.jeequan.jeepay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.SysEntitlement;
import com.jeequan.jeepay.core.entity.SysRoleEntRela;
import com.jeequan.jeepay.service.mapper.SysRoleEntRelaMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 系统角色权限关联表 服务实现类
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2020-06-13
 */
@Service
public class SysRoleEntRelaService extends ServiceImpl<SysRoleEntRelaMapper, SysRoleEntRela> {

    @Autowired
    private SysEntitlementService sysEntitlementService;

    /**
     * 根据人查询出所有权限ID集合
     */
    public List<String> selectEntIdsByUserId(Long userId, Byte isAdmin, String sysType) {


        if (isAdmin == CS.YES) {

            List<String> result = new ArrayList<>();
            sysEntitlementService.list(SysEntitlement.gw().select(SysEntitlement::getEntId).eq(SysEntitlement::getSysType, sysType).eq(SysEntitlement::getState, CS.PUB_USABLE)
            ).stream().forEach(r -> result.add(r.getEntId()));

            return result;

        } else {
            return baseMapper.selectEntIdsByUserId(userId, sysType);
        }

    }


    /**
     * 重置 角色 - 权限 关联关系
     **/
    @Transactional
    public void resetRela(String roleId, List<String> entIdList) {

        //1. 删除
        this.remove(SysRoleEntRela.gw().eq(SysRoleEntRela::getRoleId, roleId));

        //2. 插入
        for (String entId : entIdList) {
            SysRoleEntRela r = new SysRoleEntRela();
            r.setRoleId(roleId);
            r.setEntId(entId);
            this.save(r);
        }

    }


}
