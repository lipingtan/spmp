package com.spmp.common.init.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * AES 加密/解密工具类。
 * <p>
 * 预演场景，密钥硬编码。后续可升级为外部密钥管理。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public class AesEncryptUtil {

    private static final Logger log = LoggerFactory.getLogger(AesEncryptUtil.class);

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    /** AES 密钥（16 字节），预演场景硬编码 */
    private static final String AES_KEY = "spmp2026init#key";

    /** 加密标记前缀 */
    public static final String ENC_PREFIX = "ENC(";

    /** 加密标记后缀 */
    public static final String ENC_SUFFIX = ")";

    private AesEncryptUtil() {
        // 工具类禁止实例化
    }

    /**
     * AES 加密。
     *
     * @param plainText 明文
     * @return Base64 编码的密文
     */
    public static String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        try {
            SecretKeySpec keySpec = new SecretKeySpec(AES_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("AES 加密失败", e);
            throw new RuntimeException("AES 加密失败", e);
        }
    }

    /**
     * AES 解密。
     *
     * @param cipherText Base64 编码的密文
     * @return 明文
     */
    public static String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isEmpty()) {
            return cipherText;
        }
        try {
            SecretKeySpec keySpec = new SecretKeySpec(AES_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decoded = Base64.getDecoder().decode(cipherText);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("AES 解密失败", e);
            throw new RuntimeException("AES 解密失败", e);
        }
    }

    /**
     * 将明文包装为 ENC(密文) 格式。
     *
     * @param plainText 明文
     * @return ENC(密文) 格式字符串，明文为空时返回空字符串
     */
    public static String wrapEncrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return "";
        }
        return ENC_PREFIX + encrypt(plainText) + ENC_SUFFIX;
    }

    /**
     * 解析 ENC(密文) 格式并解密。
     *
     * @param wrappedText ENC(密文) 格式字符串
     * @return 明文，非 ENC 格式则原样返回
     */
    public static String unwrapDecrypt(String wrappedText) {
        if (wrappedText == null || wrappedText.isEmpty()) {
            return wrappedText;
        }
        if (wrappedText.startsWith(ENC_PREFIX) && wrappedText.endsWith(ENC_SUFFIX)) {
            String cipherText = wrappedText.substring(ENC_PREFIX.length(), wrappedText.length() - ENC_SUFFIX.length());
            return decrypt(cipherText);
        }
        return wrappedText;
    }
}
