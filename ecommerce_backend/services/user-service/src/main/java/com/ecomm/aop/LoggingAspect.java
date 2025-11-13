package com.ecomm.aop;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
@Order(1) // run before transaction aspects if any
public class LoggingAspect {

    // Pointcuts: controllers & services inside our module
    @Pointcut("within(com.ecomm.controller..*)")
    public void controllerLayer() {}

    @Pointcut("within(com.ecomm.service..*)")
    public void serviceLayer() {}

    // Around controller – logs inputs/outputs with timing
    @Around("controllerLayer()")
    public Object logController(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.nanoTime();
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        String method = sig.getDeclaringType().getSimpleName() + "." + sig.getName();

        Object[] args = pjp.getArgs();
        // Avoid logging huge payloads or secrets (mask in real apps)
        log.info("[CTRL] -> {} args={}", method, safeArgs(args));

        try {
            Object result = pjp.proceed();
            long tookMs = (System.nanoTime() - start) / 1_000_000;
            log.info("[CTRL] <- {} resultType={} took={}ms",
                    method, (result == null ? "null" : result.getClass().getSimpleName()), tookMs);
            return result;
        } catch (Throwable ex) {
            long tookMs = (System.nanoTime() - start) / 1_000_000;
            log.error("[CTRL] !! {} failed in {}ms : {}", method, tookMs, ex.toString());
            throw ex;
        }
    }

    // Around service – timing + brief args (no response dump to reduce noise)
    @Around("serviceLayer()")
    public Object logService(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.nanoTime();
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        String method = sig.getDeclaringType().getSimpleName() + "." + sig.getName();

        log.debug("[SRV] -> {}()", method);
        try {
            Object result = pjp.proceed();
            long tookMs = (System.nanoTime() - start) / 1_000_000;
            log.debug("[SRV] <- {}() took={}ms", method, tookMs);
            return result;
        } catch (Throwable ex) {
            long tookMs = (System.nanoTime() - start) / 1_000_000;
            log.error("[SRV] !! {}() failed in {}ms : {}", method, tookMs, ex.toString());
            throw ex;
        }
    }

    private String safeArgs(Object[] args) {
        // Mask common sensitive fields found in DTOs if you decide to print args deeply
        // Here we just show classes to avoid leaking PII
        return Arrays.stream(args)
                .map(a -> a == null ? "null" : a.getClass().getSimpleName())
                .toList()
                .toString();
    }
}
