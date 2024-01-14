package me.stlee321.instatube.app.distlock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class NewTransactionWrapper {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Object run(ProceedingJoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }
}
