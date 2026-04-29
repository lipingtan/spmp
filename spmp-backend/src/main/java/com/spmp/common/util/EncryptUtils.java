package com.spmp.common.util;

import com.spmp.common.exception.BusinessException;
import com.spmp.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 敏感数据加解密工具类。
 * <p>
 * 使用 AES/CBC/PKCS5Padding 模式进行加解密，密钥通过配置注入。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Component
public class EncryptUtils {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";

    @Value("${encrypt.aes-key:}")
    private String aesKey;

    /**
     * AES 加密。
     *
     * @param plainText 明文
     * @return Base64 编码的密文
     * @throws BusinessException 加密失败时抛出
     */
    public String encrypt(String plainText) {
        try {
            byte[] keyBytes = aesKey.getBytes(StandardCharsets.UTF_8);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, ALGORITHM);
            // IV 向量取密钥前 16 字节
            IvParameterSpec ivSpec = new IvParameterSpec(keyBytes, 0, 16);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("加密失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "加密失败");
        }
    }

    /**
     * AES 解密。
     *
     * @param cipherText Base64 编码的密文
     * @return 明文
     * @throws BusinessException 解密失败时抛出
     */
    public String decrypt(String cipherText) {
        try {
            byte[] keyBytes = aesKey.getBytes(StandardCharsets.UTF_8);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(keyBytes, 0, 16);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] decoded = Base64.getDecoder().decode(cipherText);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("解密失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "解密失败");
        }
    }

    /**
     * 数据脱敏。
     * <p>
     * 保留前 prefixLen 位和后 suffixLen 位，中间用 * 替换。
     * 例: mask("13812341234", 3, 4) → "138****1234"
     *
     * @param text      原始文本
     * @param prefixLen 保留前缀长度
     * @param suffixLen 保留后缀长度
     * @return 脱敏后的文本
     */
    public static String mask(String text, int prefixLen, int suffixLen) {
        if (text == null || text.length() <= prefixLen + suffixLen) {
            return text;
        }
        int maskLen = text.length() - prefixLen - suffixLen;
        StringBuilder sb = new StringBuilder(text.length());
        sb.append(text, 0, prefixLen);
        for (int i = 0; i < maskLen; i++) {
            sb.append('*');
        }
        sb.append(text.substring(text.length() - suffixLen));
        return sb.toString();
    }
}
