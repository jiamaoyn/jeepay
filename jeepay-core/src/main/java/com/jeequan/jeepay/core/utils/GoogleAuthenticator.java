package com.jeequan.jeepay.core.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base32;
import org.apache.tomcat.util.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * GoogleAuthenticator
 * 自从google出了双重身份验证后，就方便了大家，等同于有了google一个级别的安全，但是我们该怎么使用google authenticator (双重身份验证)，
 * </p>
 */
@Slf4j
public class GoogleAuthenticator {

    // 取自谷歌pam文档-我们可能不需要处理这些
    private static final int SECRET_SIZE = 10;

    // 种子， 有点像加盐
    private static final String SEED = "g2GjEvTbW3oVSV7avLBdwIHqGlUYNzKFI7izOF8GwLDVKs2m0QN7vxRs2im5MDaNCWGmcD2rvcZx";

    // 随机数字算法
    private static final String RANDOM_NUMBER_ALGORITHM = "SHA1PRNG";

    private int window_size = 1; // default 3 - max 17 (from google docs)最多可偏移的时间

    private void setWindowSize(int s) {
        if (s >= 1 && s <= 17)
            window_size = s;
    }

    /**
     * 生成密钥
     */
    public static String generateSecretKey() {
        try {
            SecureRandom sr = SecureRandom.getInstance(RANDOM_NUMBER_ALGORITHM);
            sr.setSeed(Base64.decodeBase64(SEED));
            byte[] buffer = sr.generateSeed(SECRET_SIZE);
            Base32 codec = new Base32();
            byte[] bEncodedKey = codec.encode(buffer);
            return new String(bEncodedKey);
        }catch (NoSuchAlgorithmException e) {
            log.error("生成密钥异常：", e);
        }
        return null;
    }


    /**
     * 获取QR条形码URL, 用这个生成二维码，给Google验证器扫
     */
    public static String getQRBarcodeURL(String user, String host, String secret) {
        String format = "https://www.google.com/chart?chs=200x200&chld=M%%7C0&cht=qr&chl=otpauth://totp/%s@%s%%3Fsecret%%3D%s";
        return String.format(format, user, host, secret);
    }

    /**
     * 验证码动态验证码
     * @param secret 密码，上面方法生成的
     * @param code 动态验证码
     * @param timeMsec 毫秒时间搓 System.currentTimeMillis()
     */
    public boolean checkCode(String secret, long code, long timeMsec) {
        Base32 codec = new Base32();
        byte[] decodedKey = codec.decode(secret);
        // 将unix毫秒时间转换为30秒的“窗口”，这是根据TOTP规范（有关详细信息，请参阅RFC）
        long t = (timeMsec / 1000L) / 30L;
        // 窗口用于检查最近生成的代码。您可以使用此值来调整您愿意走多远。
        for (int i = -window_size; i <= window_size; ++i) {
            long hash;
            try {
                hash = verifyCode(decodedKey, t + i);
            }catch (Exception e) {
                return false;
            }
            if (hash == code) {
                return true;
            }
        }
        // 验证代码无效。
        return false;
    }


    private static int verifyCode(byte[] key, long t) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] data = new byte[8];
        long value = t;
        for (int i = 8; i-- > 0; value >>>= 8) {
            data[i] = (byte) value;
        }
        SecretKeySpec signKey = new SecretKeySpec(key, "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(signKey);
        byte[] hash = mac.doFinal(data);
        int offset = hash[20 - 1] & 0xF;
        // 我们使用long是因为Java没有无符号int。
        long truncatedHash = 0;
        for (int i = 0; i < 4; ++i) {
            truncatedHash <<= 8;
            //我们正在处理签名字节：
            //我们只保留第一个字节。
            truncatedHash |= (hash[offset + i] & 0xFF);
        }
        truncatedHash &= 0x7FFFFFFF;
        truncatedHash %= 1000000;
        return (int) truncatedHash;
    }
}