package com.jeequan.jeepay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.SysPayDomain;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.service.mapper.SysPayDomainMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2023-12-18
 */
@Service
public class SysPayDomainService extends ServiceImpl<SysPayDomainMapper, SysPayDomain> {
    public String arbitrarily() {
        List<String> list = selectAll();
        Collections.shuffle(list);
        return list.get(0);
    }

    public List<String> selectAll() {
        List<String> list = new ArrayList<>();
        list(SysPayDomain.gw().select(SysPayDomain::getDomain).eq(SysPayDomain::getState, CS.YES)).forEach(item -> list.add(item.getDomain()));
        return list;

    }

    public IPage<SysPayDomain> selectPage(IPage iPage, SysPayDomain sysPayDomain) {
        LambdaQueryWrapper<SysPayDomain> wrapper = SysPayDomain.gw();
        if (StringUtils.isNotEmpty(sysPayDomain.getIp())) {
            wrapper.eq(SysPayDomain::getIp, sysPayDomain.getIp());
        }
        if (StringUtils.isNotEmpty(sysPayDomain.getDomain())) {
            wrapper.eq(SysPayDomain::getDomain, sysPayDomain.getDomain());
        }
        if (sysPayDomain.getState() != null) {
            wrapper.eq(SysPayDomain::getState, sysPayDomain.getState());
        }
        wrapper.orderByDesc(SysPayDomain::getCreatedAt);
        IPage<SysPayDomain> pages = this.page(iPage, wrapper);
        return pages;
    }

    public void addPayDomain(SysPayDomain sysPayDomain) {
        boolean saveResult = save(sysPayDomain);
        if (!saveResult) {
            throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_CREATE);
        }
    }
    @Transactional(rollbackFor = Exception.class)
    public void removeByIdKey(String id) {
        try {
            // 0.当前商户是否存在
            SysPayDomain sysPayDomain = getById(id);
            if (sysPayDomain == null) {
                throw new BizException("该支付域名不存在");
            }
            // 7.删除当前商户
            boolean removeMchInfo = removeById(id);
            if (!removeMchInfo) {
                throw new BizException("删除当前商户失败");
            }
        } catch (Exception e) {
            throw new BizException(e.getMessage());
        }
    }

    public SysPayDomain selectById(String id) {
        SysPayDomain sysPayDomain = this.getById(id);
        if (sysPayDomain == null) {
            return null;
        }
        return sysPayDomain;
    }
}
