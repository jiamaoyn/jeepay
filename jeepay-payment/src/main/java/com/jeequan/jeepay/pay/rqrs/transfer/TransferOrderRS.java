package com.jeequan.jeepay.pay.rqrs.transfer;

import com.jeequan.jeepay.core.entity.TransferOrder;
import com.jeequan.jeepay.pay.rqrs.AbstractRS;
import lombok.Data;
import org.springframework.beans.BeanUtils;

/*
 * 创建订单(统一订单) 响应参数
 * @date 2021/6/8 17:34
 */
@Data
public class TransferOrderRS extends AbstractRS {


    /**
     * 转账单号
     **/
    private String transferId;

    /**
     * 商户单号
     **/
    private String mchOrderNo;

    /**
     * 转账金额
     **/
    private Long amount;

    /**
     * 收款账号
     */
    private String accountNo;

    /**
     * 收款人姓名
     */
    private String accountName;

    /**
     * 收款人开户行名称
     */
    private String bankName;

    /**
     * 状态
     **/
    private Byte state;

    /**
     * 渠道退款单号
     **/
    private String channelOrderNo;

    /**
     * 渠道返回错误代码
     **/
    private String errCode;

    /**
     * 渠道返回错误信息
     **/
    private String errMsg;

    public static TransferOrderRS buildByRecord(TransferOrder record) {

        if (record == null) {
            return null;
        }

        TransferOrderRS result = new TransferOrderRS();
        BeanUtils.copyProperties(record, result);

        return result;
    }


}
