package com.jeequan.jeepay.core.model.params.plspay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.model.params.NormalMchParams;
import com.jeequan.jeepay.core.utils.StringKit;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/*
 * 计全支付plus， 普通商户参数定义
 *
 * @author yurong
 * @site https://www.jeequan.com
 * @date 2022/8/11 14:32
 */
@Data
public class PlspayNormalMchParams extends NormalMchParams {

    /**
     * RSA2: 支付网关公钥
     */
    public String rsa2PayPublicKey;
    /**
     * 商户号
     */
    private String merchantNo;
    /**
     * 应用ID
     */
    private String appId;
    /**
     * 签名方式
     **/
    private String signType;
    /**
     * md5秘钥
     */
    private String appSecret;
    /**
     * RSA2: 应用私钥
     */
    private String rsa2AppPrivateKey;

    @Override
    public String deSenData() {

        PlspayNormalMchParams mchParams = this;
        if (StringUtils.isNotBlank(this.appSecret)) {
            mchParams.setAppSecret(StringKit.str2Star(this.appSecret, 4, 4, 6));
        }
        return ((JSONObject) JSON.toJSON(mchParams)).toJSONString();
    }

}
