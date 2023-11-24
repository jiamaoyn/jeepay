package com.jeequan.task.channel;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.beans.RequestKitBean;
import com.jeequan.task.service.ConfigContextQueryService;
import com.jeequan.task.util.ChannelCertConfigKitBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.io.File;

/*
 * 实现回调接口抽象类
 *
 * @author zx
 * @site https://www.jeequan.com
 * @date 2022/12/30 10:18
 */
public abstract class AbstractTransferNoticeService implements ITransferNoticeService {

    @Autowired
    protected ConfigContextQueryService configContextQueryService;
    @Autowired
    private RequestKitBean requestKitBean;
    @Autowired
    private ChannelCertConfigKitBean channelCertConfigKitBean;

    @Override
    public ResponseEntity doNotifyOrderNotExists(HttpServletRequest request) {
        return textResp("order not exists");
    }

    /**
     * 文本类型的响应数据
     **/
    protected ResponseEntity textResp(String text) {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.TEXT_HTML);
        return new ResponseEntity(text, httpHeaders, HttpStatus.OK);
    }

    /**
     * json类型的响应数据
     **/
    protected ResponseEntity jsonResp(Object body) {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity(body, httpHeaders, HttpStatus.OK);
    }


    /**
     * request.getParameter 获取参数 并转换为JSON格式
     **/
    protected JSONObject getReqParamJSON() {
        return requestKitBean.getReqParamJSON();
    }

    /**
     * request.getParameter 获取参数 并转换为JSON格式
     **/
    protected String getReqParamFromBody() {
        return requestKitBean.getReqParamFromBody();
    }

    /**
     * 获取文件路径
     **/
    protected String getCertFilePath(String certFilePath) {
        return channelCertConfigKitBean.getCertFilePath(certFilePath);
    }

    /**
     * 获取文件File对象
     **/
    protected File getCertFile(String certFilePath) {
        return channelCertConfigKitBean.getCertFile(certFilePath);
    }

}
