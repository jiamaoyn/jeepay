package com.jeequan.jeepay.core.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jeequan.jeepay.core.model.BaseModel;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2023-11-30
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_telegram_chat")
public class TelegramChat extends BaseModel implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     * 商户号
     */
    private String mchNo;

    /**
     * 商户状态: 0-停用, 1-正常
     */
    private Byte state;

    private String chatId;
    public static LambdaQueryWrapper<TelegramChat> gw() {
        return new LambdaQueryWrapper<>();
    }

}
