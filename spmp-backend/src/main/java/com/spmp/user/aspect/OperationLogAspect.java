package com.spmp.user.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spmp.user.annotation.OperationLog;
import com.spmp.user.domain.entity.OperationLogDO;
import com.spmp.user.service.OperationLogService;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * 操作日志切面。
 * <p>
 * 支持通过 SpEL 表达式在方法执行前查询原始数据，实现变更前后数据对比。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    private final OperationLogService operationLogService;
    private final ObjectMapper objectMapper;
    private final BeanFactory beanFactory;

    /** ThreadLocal 存储变更前数据 */
    private static final ThreadLocal<String> BEFORE_DATA = new ThreadLocal<>();

    private static final ExpressionParser PARSER = new SpelExpressionParser();

    public OperationLogAspect(OperationLogService operationLogService,
                              ObjectMapper objectMapper,
                              BeanFactory beanFactory) {
        this.operationLogService = operationLogService;
        this.objectMapper = objectMapper;
        this.beanFactory = beanFactory;
    }

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        Throwable error = null;

        try {
            // 方法执行前：通过 SpEL 查询原始数据
            queryBeforeData(operationLog, joinPoint);

            result = joinPoint.proceed();
            return result;
        } catch (Throwable t) {
            error = t;
            throw t;
        } finally {
            try {
                long costTime = System.currentTimeMillis() - startTime;
                saveLog(operationLog, joinPoint, result, error, costTime);
            } catch (Exception e) {
                log.warn("记录操作日志失败: {}", e.getMessage());
            } finally {
                BEFORE_DATA.remove();
            }
        }
    }

    /**
     * 通过 SpEL 表达式查询变更前数据。
     */
    private void queryBeforeData(OperationLog operationLog, ProceedingJoinPoint joinPoint) {
        String spel = operationLog.beforeQuerySpEL();
        if (spel == null || spel.isEmpty()) {
            return;
        }
        try {
            Object beforeData = evaluateSpEL(spel, joinPoint);
            if (beforeData != null) {
                String json = objectMapper.writeValueAsString(beforeData);
                BEFORE_DATA.set(json);
            }
        } catch (Exception e) {
            log.warn("查询变更前数据失败, SpEL={}, 原因: {}", spel, e.getMessage());
        }
    }

    /**
     * 解析 SpEL 表达式。
     */
    private Object evaluateSpEL(String spel, ProceedingJoinPoint joinPoint) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        // 注册方法参数
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }
        // 注册 Spring Bean（支持 @beanName.method() 语法）
        context.setBeanResolver(new BeanFactoryResolver(beanFactory));
        return PARSER.parseExpression(spel).getValue(context);
    }

    private void saveLog(OperationLog annotation, ProceedingJoinPoint joinPoint,
                         Object result, Throwable error, long costTime) {
        OperationLogDO logDO = new OperationLogDO();
        logDO.setModule(annotation.module());
        logDO.setOperationType(annotation.type());
        logDO.setDescription(annotation.description());
        logDO.setCostTime(costTime);
        logDO.setOperationTime(new Date());

        // 操作人信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getDetails() instanceof Claims) {
            Claims claims = (Claims) auth.getDetails();
            Object userId = claims.get("userId");
            if (userId instanceof Number) {
                logDO.setOperatorId(((Number) userId).longValue());
            }
            logDO.setOperatorName(claims.getSubject());
        }

        // 请求信息
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            logDO.setRequestMethod(request.getMethod());
            logDO.setRequestUrl(request.getRequestURI());
            logDO.setOperationIp(getClientIp(request));
        }

        // 请求参数（脱敏）
        try {
            String params = objectMapper.writeValueAsString(joinPoint.getArgs());
            params = params.replaceAll("\"password\":\"[^\"]*\"", "\"password\":\"******\"");
            if (params.length() > 2000) {
                params = params.substring(0, 2000);
            }
            logDO.setRequestParams(params);
        } catch (Exception e) {
            logDO.setRequestParams("序列化失败");
        }

        // 响应结果
        if (error != null) {
            logDO.setResponseResult("异常: " + error.getClass().getSimpleName() + " - " + error.getMessage());
        } else if (result != null) {
            try {
                String resultStr = objectMapper.writeValueAsString(result);
                if (resultStr.length() > 2000) {
                    resultStr = resultStr.substring(0, 2000);
                }
                logDO.setResponseResult(resultStr);
            } catch (Exception e) {
                logDO.setResponseResult("序列化失败");
            }
        }

        // 变更前数据
        String beforeDataJson = BEFORE_DATA.get();
        if (beforeDataJson != null) {
            if (beforeDataJson.length() > 2000) {
                beforeDataJson = beforeDataJson.substring(0, 2000);
            }
            logDO.setBeforeData(beforeDataJson);
        }

        operationLogService.saveLog(logDO);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
