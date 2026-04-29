package com.spmp.common.util;

import com.spmp.common.exception.BusinessException;
import com.spmp.common.exception.ErrorCode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 日期工具类。
 * <p>
 * 提供常用日期格式化和转换方法，使用 ThreadLocal 保证线程安全。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public final class DateUtils {

    /** 日期格式：yyyy-MM-dd */
    public static final String YYYY_MM_DD = "yyyy-MM-dd";

    /** 日期时间格式：yyyy-MM-dd HH:mm:ss */
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    /**
     * ThreadLocal 缓存 SimpleDateFormat 实例，兼顾线程安全和性能。
     */
    private static final ThreadLocal<Map<String, SimpleDateFormat>> DATE_FORMAT_LOCAL =
            ThreadLocal.withInitial(HashMap::new);

    private DateUtils() {
        // 工具类禁止实例化
    }

    /**
     * 获取指定 pattern 的 SimpleDateFormat（线程安全）。
     *
     * @param pattern 日期格式
     * @return SimpleDateFormat 实例
     */
    private static SimpleDateFormat getDateFormat(String pattern) {
        Map<String, SimpleDateFormat> map = DATE_FORMAT_LOCAL.get();
        return map.computeIfAbsent(pattern, SimpleDateFormat::new);
    }

    /**
     * 将 Date 对象格式化为指定模式的字符串。
     *
     * @param date    日期对象
     * @param pattern 日期格式
     * @return 格式化后的日期字符串
     */
    public static String format(Date date, String pattern) {
        if (date == null) {
            return null;
        }
        return getDateFormat(pattern).format(date);
    }

    /**
     * 将字符串解析为 Date 对象。
     *
     * @param dateStr 日期字符串
     * @param pattern 日期格式
     * @return 解析后的 Date 对象
     * @throws BusinessException 格式不匹配时抛出
     */
    public static Date parse(String dateStr, String pattern) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            return getDateFormat(pattern).parse(dateStr);
        } catch (ParseException e) {
            throw new BusinessException(ErrorCode.PARAM_INVALID,
                    "日期格式错误: " + dateStr + "，期望格式: " + pattern);
        }
    }

    /**
     * 返回当前时间。
     *
     * @return 当前时间的 Date 对象
     */
    public static Date now() {
        return new Date();
    }
}
