package com.spmp.common.security;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

/**
 * 数据权限拦截器。
 * <p>
 * 在 SQL 执行前根据用户数据权限级别自动追加 WHERE 条件，
 * 实现五级数据权限隔离（全部 → 片区 → 小区 → 楼栋 → 仅本人）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
public class DataPermissionInterceptor implements InnerInterceptor {

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms,
                            Object parameter, RowBounds rowBounds,
                            ResultHandler resultHandler, BoundSql boundSql)
            throws SQLException {
        // 检查 Mapper 方法是否标注 @DataPermission 注解
        DataPermission annotation = getDataPermissionAnnotation(ms);
        if (annotation == null) {
            return;
        }

        // 从 ThreadLocal 获取当前用户的数据权限上下文
        DataPermissionContext context = DataPermissionContext.getContext();
        if (context == null || context.getLevel() == null
                || context.getLevel() == DataPermissionLevel.ALL) {
            return;
        }

        // 根据权限级别追加 SQL WHERE 条件
        String additionalCondition = buildCondition(annotation, context);
        if (additionalCondition != null && !additionalCondition.isEmpty()) {
            String originalSql = boundSql.getSql();
            String newSql = addWhereCondition(originalSql, additionalCondition);
            // 通过反射修改 BoundSql 中的 sql
            try {
                java.lang.reflect.Field sqlField = BoundSql.class.getDeclaredField("sql");
                sqlField.setAccessible(true);
                sqlField.set(boundSql, newSql);
            } catch (Exception e) {
                log.error("数据权限拦截器修改 SQL 失败", e);
            }
        }
    }

    /**
     * 获取 Mapper 方法上的 @DataPermission 注解。
     */
    private DataPermission getDataPermissionAnnotation(MappedStatement ms) {
        try {
            String id = ms.getId();
            int lastDot = id.lastIndexOf('.');
            String className = id.substring(0, lastDot);
            String methodName = id.substring(lastDot + 1);
            Class<?> mapperClass = Class.forName(className);
            for (Method method : mapperClass.getMethods()) {
                if (method.getName().equals(methodName)) {
                    DataPermission annotation = method.getAnnotation(DataPermission.class);
                    if (annotation != null) {
                        return annotation;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("获取 @DataPermission 注解失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 根据权限级别构建 SQL 条件。
     * <p>
     * 支持 SELF 级别和 scopeMap 遍历的 IN 查询。
     */
    private String buildCondition(DataPermission annotation, DataPermissionContext context) {
        // SELF 级别：按用户名过滤
        if (context.getLevel() == DataPermissionLevel.SELF) {
            String selfField = annotation.selfField();
            String username = context.getUsername();
            if (username != null && !username.isEmpty()) {
                return selfField + " = '" + username + "'";
            }
            return null;
        }

        // scopeMap 遍历：动态拼接 IN 条件
        Map<String, Set<Long>> scopeMap = context.getScopeMap();
        if (scopeMap == null || scopeMap.isEmpty()) {
            return null;
        }

        StringJoiner orJoiner = new StringJoiner(" OR ");
        for (Map.Entry<String, Set<Long>> entry : scopeMap.entrySet()) {
            String field = entry.getKey();
            Set<Long> ids = entry.getValue();
            if (ids != null && !ids.isEmpty()) {
                StringJoiner idJoiner = new StringJoiner(",");
                for (Long id : ids) {
                    idJoiner.add(String.valueOf(id));
                }
                orJoiner.add(field + " IN (" + idJoiner.toString() + ")");
            }
        }

        String result = orJoiner.toString();
        if (result.isEmpty()) {
            return null;
        }
        // 多个 OR 条件时加括号
        if (scopeMap.size() > 1) {
            return "(" + result + ")";
        }
        return result;
    }

    /**
     * 在原始 SQL 中追加 WHERE 条件。
     */
    private String addWhereCondition(String originalSql, String condition) {
        String upperSql = originalSql.toUpperCase();
        int whereIndex = upperSql.lastIndexOf("WHERE");
        if (whereIndex > 0) {
            // 已有 WHERE 子句，追加 AND 条件
            return originalSql + " AND " + condition;
        } else {
            // 无 WHERE 子句，添加 WHERE
            int orderByIndex = upperSql.lastIndexOf("ORDER BY");
            int limitIndex = upperSql.lastIndexOf("LIMIT");
            int groupByIndex = upperSql.lastIndexOf("GROUP BY");

            // 找到最早的后缀关键字位置
            int insertPos = originalSql.length();
            if (orderByIndex > 0) {
                insertPos = Math.min(insertPos, orderByIndex);
            }
            if (limitIndex > 0) {
                insertPos = Math.min(insertPos, limitIndex);
            }
            if (groupByIndex > 0) {
                insertPos = Math.min(insertPos, groupByIndex);
            }

            return originalSql.substring(0, insertPos)
                    + " WHERE " + condition + " "
                    + originalSql.substring(insertPos);
        }
    }
}
