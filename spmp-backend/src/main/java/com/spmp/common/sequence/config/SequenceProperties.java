package com.spmp.common.sequence.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 序列配置。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "sequence")
public class SequenceProperties {

    private Map<String, Integer> step = new HashMap<>();

    private Mode mode = new Mode();

    @Data
    public static class Mode {
        private String billing = "legacy";
        private String workorder = "legacy";
    }
}
