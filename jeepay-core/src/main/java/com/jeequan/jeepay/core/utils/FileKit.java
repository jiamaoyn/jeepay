package com.jeequan.jeepay.core.utils;

import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.exception.BizException;

/*
 * 文件工具类
 * @date 2021/6/8 16:50
 */
public class FileKit {


    /**
     * 获取文件的后缀名
     *
     * @param appendDot 是否拼接.
     * @return
     */
    public static String getFileSuffix(String fullFileName, boolean appendDot) {
        if (fullFileName == null || fullFileName.indexOf(".") < 0 || fullFileName.length() <= 1) {
            return "";
        }
        return (appendDot ? "." : "") + fullFileName.substring(fullFileName.lastIndexOf(".") + 1);
    }


    /**
     * 获取有效的图片格式， 返回null： 不支持的图片类型
     **/
    public static String getImgSuffix(String filePath) {

        String suffix = getFileSuffix(filePath, false).toLowerCase();
        if (CS.ALLOW_UPLOAD_IMG_SUFFIX.contains(suffix)) {
            return suffix;
        }
        throw new BizException("不支持的图片类型");
    }

}
