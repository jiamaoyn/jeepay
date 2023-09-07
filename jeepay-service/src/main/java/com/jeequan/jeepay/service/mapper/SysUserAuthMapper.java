package com.jeequan.jeepay.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jeequan.jeepay.core.entity.SysUserAuth;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 操作员认证表 Mapper 接口
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2020-06-13
 */
public interface SysUserAuthMapper extends BaseMapper<SysUserAuth> {

    SysUserAuth selectByLogin(@Param("identifier") String identifier,
                              @Param("identityType") Byte identityType, @Param("sysType") String sysType);

}
