package me.stlee321.instatube.app.distlock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class DistLockAspect {

    private final RedissonClient redissonClient;
    private final NewTransactionWrapper wrapper;

    public DistLockAspect(RedissonClient redissonClient, NewTransactionWrapper wrapper) {
        this.redissonClient = redissonClient;
        this.wrapper = wrapper;
    }
    @Around("@annotation(me.stlee321.instatube.app.distlock.DistLock)")
    public Object distributedLock(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature)point.getSignature();
        Method method = signature.getMethod();
        DistLock distLock = method.getAnnotation(DistLock.class);
        RLock lock = redissonClient.getLock(distLock.keyName());

        Object ret = null;
        try {
            boolean getLock = lock.tryLock(100, 2, TimeUnit.SECONDS);
            if(!getLock) {
                throw new Exception("lock fail");
            }
            ret = wrapper.run(point);
        }catch(Exception e) {
        }finally {
            lock.unlock();
        }
        return ret;
    }
}
