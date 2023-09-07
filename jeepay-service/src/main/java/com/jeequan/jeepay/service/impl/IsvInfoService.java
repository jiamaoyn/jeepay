package com.jeequan.jeepay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.IsvInfo;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.entity.PayInterfaceConfig;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.service.mapper.IsvInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 服务商信息表 服务实现类
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2021-04-27
 */
@Service
public class IsvInfoService extends ServiceImpl<IsvInfoMapper, IsvInfo> {

    @Autowired
    private MchInfoService mchInfoService;

    @Autowired
    private IsvInfoService isvInfoService;

    @Autowired
    private PayInterfaceConfigService payInterfaceConfigService;

    @Transactional
    public void removeByIsvNo(String isvNo) {
        // 0.当前服务商是否存在
        IsvInfo isvInfo = isvInfoService.getById(isvNo);
        if (isvInfo == null) {
            throw new BizException("该服务商不存在");
        }

        // 1.查询当前服务商下是否存在商户
        int mchCount = mchInfoService.count(MchInfo.gw().eq(MchInfo::getIsvNo, isvNo).eq(MchInfo::getType, CS.MCH_TYPE_ISVSUB));
        if (mchCount > 0) {
            throw new BizException("该服务商下存在商户，不可删除");
        }

        // 2.删除当前服务商支付接口配置参数
        payInterfaceConfigService.remove(PayInterfaceConfig.gw()
                .eq(PayInterfaceConfig::getInfoId, isvNo)
                .eq(PayInterfaceConfig::getInfoType, CS.INFO_TYPE_ISV)
        );

        // 3.删除该服务商
        boolean remove = isvInfoService.removeById(isvNo);
        if (!remove) {
            throw new BizException("删除服务商失败");
        }
    }
}
