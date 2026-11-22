package com.example.help_bridge.common.audit;

import com.example.help_bridge.common.logging.SensitiveDataMasker;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Arrays;
import java.util.regex.Pattern;

@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 10)
public class AuditAspect {

    private static final Pattern SENSITIVE_PARAM_NAME = Pattern.compile(
            "(?i).*(password|token|secret|cvv|cvc|bankdetail|iban).*");

    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public AuditAspect(AuditLogService auditLogService, @Autowired(required = false) ObjectMapper objectMapper) {
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper().findAndRegisterModules();
    }

    @Pointcut("@annotation(com.example.help_bridge.common.audit.AuditAction) || @within(com.example.help_bridge.common.audit.AuditAction)")
    public void auditPointcut() {
    }

    @Around("auditPointcut()")
    public Object audit(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<?> targetClass = joinPoint.getTarget() == null
                ? signature.getDeclaringType()
                : AopUtils.getTargetClass(joinPoint.getTarget());
        Method method = AopUtils.getMostSpecificMethod(signature.getMethod(), targetClass);
        AuditAction auditAction = AnnotatedElementUtils.findMergedAnnotation(method, AuditAction.class);
        if (auditAction == null) {
            auditAction = AnnotatedElementUtils.findMergedAnnotation(targetClass, AuditAction.class);
        }
        String action = resolveAction(auditAction, signature.getName());

        String username = resolveUsername();
        String methodName = resolveMethodName(joinPoint, signature);
        String arguments = serializeArguments(joinPoint, signature);
        Instant occurredAt = Instant.now();

        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable businessEx) {
            try {
                auditLogService.save(new AuditLogCommand(
                        action,
                        username,
                        methodName,
                        arguments,
                        null,
                        occurredAt,
                        false,
                        businessEx.getClass().getName(),
                        businessEx.getMessage()
                ));
            } catch (Throwable auditEx) {
                businessEx.addSuppressed(auditEx);
            }
            throw businessEx;
        }

        String resultString = null;
        if (!Void.TYPE.equals(signature.getReturnType()) && result != null) {
            resultString = serializeResult(result);
        }

        auditLogService.save(new AuditLogCommand(
                action,
                username,
                methodName,
                arguments,
                resultString,
                occurredAt,
                true,
                null,
                null
        ));

        return result;
    }

    private String resolveAction(AuditAction auditAction, String methodName) {
        if (auditAction == null) {
            return methodName;
        }
        if (!auditAction.value().isBlank()) {
            return auditAction.value();
        }
        if (!auditAction.action().isBlank()) {
            return auditAction.action();
        }
        return methodName;
    }

    private String resolveMethodName(ProceedingJoinPoint joinPoint, MethodSignature signature) {
        String className = joinPoint.getTarget() != null
                ? joinPoint.getTarget().getClass().getSimpleName()
                : signature.getDeclaringType().getSimpleName();
        int cglibIndex = className.indexOf("$$");
        if (cglibIndex != -1) {
            className = className.substring(0, cglibIndex);
        }
        return className + "." + signature.getName();
    }

    private String resolveUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return "anonymous";
        }
        String name = authentication.getName();
        if (name == null || name.isBlank()) {
            return "anonymous";
        }
        return name;
    }

    private String serializeArguments(ProceedingJoinPoint joinPoint, MethodSignature signature) {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return "[]";
        }
        Object[] processedArgs = Arrays.copyOf(args, args.length);
        String[] paramNames = signature.getParameterNames();
        if (paramNames != null && paramNames.length == processedArgs.length) {
            for (int i = 0; i < paramNames.length; i++) {
                if (paramNames[i] != null && SENSITIVE_PARAM_NAME.matcher(paramNames[i]).matches()) {
                    processedArgs[i] = "****";
                }
            }
        }
        try {
            return SensitiveDataMasker.mask(objectMapper.writeValueAsString(processedArgs));
        } catch (Throwable t) {
            return SensitiveDataMasker.mask(Arrays.deepToString(processedArgs));
        }
    }

    private String serializeResult(Object result) {
        if (result == null) {
            return null;
        }
        if (result instanceof String stringResult) {
            return SensitiveDataMasker.mask(stringResult);
        }
        try {
            return SensitiveDataMasker.mask(objectMapper.writeValueAsString(result));
        } catch (Throwable t) {
            return SensitiveDataMasker.mask(String.valueOf(result));
        }
    }
}
