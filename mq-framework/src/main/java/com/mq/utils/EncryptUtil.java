package com.mq.utils;

import cn.hutool.core.codec.Base64;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * 安全相关的工具类，包括各种加密算法
 */
@Slf4j
public class EncryptUtil {

    private static  final String charset = "UTF-8";
    /**
     * 获得指定字符串的MD5码
     * @param key 字符串
     * @return MD5
     */
    public static String md5(String key){
        return DigestUtils.md5Hex(key.getBytes());
    }
    /**
     * 获得指定字符串的sha1码
     * @param key 字符串
     * @return sha1
     */
    public static String sha1(String key) {
        return DigestUtils.sha1Hex(key.getBytes());
    }
    /**
     * 获得指定字符串的sha256码
     * @param key 字符串
     * @return sha256
     */
    public static String sha256(String key) throws Exception {
        return DigestUtils.sha256Hex(key.getBytes(charset));
    }
    /**
     * 对给定的字符串做base64编码
     *
     * @param key 源字符串
     * @return base64编码
     */
    public static String base64Encode(String key){
       return Base64.encode(key.getBytes());
    }
    /**
     * base64编码字符解码
     *
     * @param key 已编码字符串
     * @return base64编码
     */
    public static String base64Dncode(String key)throws Exception{
        return new String(Base64.decode(key.getBytes()),charset);
    }

}
