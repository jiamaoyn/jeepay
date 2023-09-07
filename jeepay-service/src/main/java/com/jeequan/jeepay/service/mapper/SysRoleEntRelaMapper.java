package com.jeequan.jeepay.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jeequan.jeepay.core.entity.SysRoleEntRela;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 系统角色权限关联表 Mapper 接口
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2020-06-13
 */
public interface SysRoleEntRelaMapper extends BaseMapper<SysRoleEntRela> {

    List<String> selectEntIdsByUserId(@Param("userId") Long userId, @Param("sysType") String sysType);

}
