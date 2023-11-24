package com.jeequan.task.rqrs.payorder;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.task.rqrs.AbstractMchAppRQ;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.BeanUtils;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/*
 * 创建订单请求参数对象
 * 聚合支付接口（统一下单）
 * @date 2021/6/8 17:33
 */
@Data
public class UnifiedOrderRQ extends AbstractMchAppRQ {

    /**
     * 商户订单号
     **/
    @NotBlank(message = "商户订单号不能为空")
    private String mchOrderNo;

    /**
     * 支付方式  如： wxpay_jsapi,alipay_wap等
     **/
    @NotBlank(message = "支付方式不能为空")
    private String wayCode;

    /**
     * 支付金额， 单位：分
     **/
    @NotNull(message = "支付金额不能为空")
    @Min(value = 1, message = "支付金额不能为空")
    private Long amount;

    /**
     * 货币代码
     **/
    @NotBlank(message = "货币代码不能为空")
    private String currency;

    /**
     * 客户端IP地址
     **/
    private String clientIp;

    /**
     * 商品标题
     **/
    @NotBlank(message = "商品标题不能为空")
    private String subject;

    /**
     * 商品描述信息
     **/
    @NotBlank(message = "商品描述信息不能为空")
    private String body;

    /**
     * 异步通知地址
     **/
    private String notifyUrl;

    /**
     * 跳转通知地址
     **/
    private String returnUrl;

    /**
     * 订单失效时间, 单位：秒
     **/
    private Integer expiredTime;

    /**
     * 特定渠道发起额外参数
     **/
    private String channelExtra;

    /**
     * 商户扩展参数
     **/
    private String extParam;

    /**
     * 分账模式： 0-该笔订单不允许分账, 1-支付成功按配置自动完成分账, 2-商户手动分账(解冻商户金额)
     **/
    @Range(min = 0, max = 2, message = "分账模式设置值有误")
    private Byte divisionMode;

    /**
     * 返回真实的bizRQ
     **/
    public UnifiedOrderRQ buildBizRQ() {
        return this;
    }

    /**
     * 获取渠道用户ID
     **/
    @JSONField(serialize = false)
    public String getChannelUserId() {
        return null;
    }

}
