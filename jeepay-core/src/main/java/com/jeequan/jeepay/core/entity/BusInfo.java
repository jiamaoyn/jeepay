package com.jeequan.jeepay.core.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2023-12-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_bus_info")
public class BusInfo implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     * 商户号
     */
    private String busNo;

    /**
     * 商户名称
     */
    private String busName;

    /**
     * 商户简称
     */
    private String busShortName;

    /**
     * 商户私钥
     */
    private String secret;

    /**
     * 商户状态: 0-停用, 1-正常
     */
    private Byte state;

    /**
     * 商户备注
     */
    private String remark;

    /**
     * 初始用户ID（创建商户时，允许商户登录的用户）
     */
    private Long initUserId;

    /**
     * 创建者用户ID
     */
    private Long createdUid;

    /**
     * 创建者姓名
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    /**
     * 谷歌密钥
     */
    private String googleKey;

    public static LambdaQueryWrapper<BusInfo> gw() {
        return new LambdaQueryWrapper<>();
    }
}
