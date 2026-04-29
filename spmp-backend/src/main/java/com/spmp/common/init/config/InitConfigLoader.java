package com.spmp.common.init.config;

import com.spmp.common.init.util.AesEncryptUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 初始化配置加载器。
 * <p>
 * 在 Spring Boot 启动时加载 config/init-config.yml，
 * 解密 ENC() 包裹的密码字段，覆盖默认的数据库和 Redis 配置。
 * 通过 EnvironmentPostProcessor 实现，在 Bean 创建之前生效。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public class InitConfigLoader implements EnvironmentPostProcessor {

    private static final String INIT_CONFIG_PATH = "config" + File.separator + "init-config.yml";
    private static final String PROPERTY_SOURCE_NAME = "initConfig";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        File configFile = new File(INIT_CONFIG_PATH);
        if (!configFile.exists()) {
            return;
        }

        try (FileInputStream fis = new FileInputStream(configFile)) {
            Yaml yaml = new Yaml();
            Map<String, Object> yamlMap = yaml.load(fis);
            if (yamlMap == null) {
                return;
            }

            Map<String, Object> flatProperties = new HashMap<>(16);
            flattenMap("", yamlMap, flatProperties);

            // 解密 ENC() 包裹的密码字段
            for (Map.Entry<String, Object> entry : flatProperties.entrySet()) {
                if (entry.getValue() instanceof String) {
                    String value = (String) entry.getValue();
                    if (value.startsWith(AesEncryptUtil.ENC_PREFIX) && value.endsWith(AesEncryptUtil.ENC_SUFFIX)) {
                        flatProperties.put(entry.getKey(), AesEncryptUtil.unwrapDecrypt(value));
                    }
                }
            }

            // 添加为高优先级 PropertySource
            environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, flatProperties));
        } catch (Exception e) {
            // 加载失败不阻止启动，记录到 stderr（此时日志框架可能未初始化）
            System.err.println("[InitConfigLoader] 加载 init-config.yml 失败: " + e.getMessage());
        }
    }

    /**
     * 将嵌套 Map 展平为 Spring 属性格式（如 spring.datasource.url）。
     */
    @SuppressWarnings("unchecked")
    private void flattenMap(String prefix, Map<String, Object> map, Map<String, Object> result) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                flattenMap(key, (Map<String, Object>) value, result);
            } else {
                result.put(key, value);
            }
        }
    }
}
