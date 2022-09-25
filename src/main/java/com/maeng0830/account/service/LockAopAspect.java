package com.maeng0830.account.service;

import com.maeng0830.account.aop.AccountLockIdInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LockAopAspect {
    private final LockService lockService;

    @Around("@annotation(com.maeng0830.account.aop.AccountLock) && args(request)")
    public Object aroundMethod(ProceedingJoinPoint pjp, AccountLockIdInterface request) throws Throwable {
        // lock ��� �õ�
        lockService.lock(request.getAccountNumber());
        try {
            // before
            return pjp.proceed();
            // after
        } finally {
            // lock ����
            lockService.unlock(request.getAccountNumber());
        }
    }
}
