package com.spmp.notice.support;

import com.spmp.common.exception.BusinessException;
import com.spmp.notice.constant.NoticeConstants;
import com.spmp.notice.constant.NoticeErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * 公告富文本过滤器。
 */
@Component
public class NoticeRichTextSanitizer {

    private static final Pattern SCRIPT_STYLE_TAG = Pattern.compile("(?is)<(script|style)(.*?)>(.*?)</(script|style)>");
    private static final Pattern EVENT_ATTR = Pattern.compile("(?i)\\s(on\\w+)\\s*=\\s*('|\").*?('|\")");
    private static final Pattern JS_PROTOCOL = Pattern.compile("(?i)javascript:");

    public String sanitize(String content) {
        if (!StringUtils.hasText(content)) {
            throw new BusinessException(NoticeErrorCode.NOTICE_PARAM_INVALID.getCode(), "公告内容不能为空");
        }
        if (content.length() > NoticeConstants.MAX_CONTENT_LENGTH) {
            throw new BusinessException(NoticeErrorCode.NOTICE_RICH_TEXT_INVALID.getCode(),
                    "公告内容长度超限，最大" + NoticeConstants.MAX_CONTENT_LENGTH + "字符");
        }

        String filtered = SCRIPT_STYLE_TAG.matcher(content).replaceAll("");
        filtered = EVENT_ATTR.matcher(filtered).replaceAll("");
        filtered = JS_PROTOCOL.matcher(filtered).replaceAll("");
        return filtered;
    }
}
