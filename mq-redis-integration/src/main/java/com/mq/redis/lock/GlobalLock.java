package com.mq.redis.lock;

import org.springframework.data.redis.core.RedisTemplate;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * redis锁实例
 */
public class GlobalLock {

    /*尝试获取锁默认超时时间*/
    private final static int TYR_LOCK_TIMEOUT = 30 * 1000; // 30s
    /*每次抢占锁的时间间隔*/
    private  static long GET_LOCK_WAIT_TIMEOUT = 150; // 200ms
    /*本地最大并发数*/
    private final static int MAX_PERMITS = 1;
    /*信号量，用于限制本地抢占锁，减少没必要的与redis交互*/
    private final Semaphore sema = new Semaphore(MAX_PERMITS);
    /*锁的Key*/
    private final String globalKey;
    /*锁的超时时间*/
    private long expired;
    /*redis客户端实例*/
    RedisTemplate<String, String> redis;


    /**
     * @param redis 客户端实例
     * @param globalKey 锁key
     * @param expired  锁的过期时间
     */
    public GlobalLock(RedisTemplate<String, String> redis, String globalKey, long expired) {
        this.redis = redis;
        this.globalKey = globalKey;
        this.expired = expired;
    }

    /**
     * @param redis 客户端实例
     * @param globalKey 锁key
     * @param expired  锁的过期时间
     * @param acquirePeriod 抢占锁的时间频率ms
     */
    public GlobalLock(RedisTemplate<String, String> redis, String globalKey, long expired,Long acquirePeriod) {
        this.redis = redis;
        this.globalKey = globalKey;
        this.expired = expired;
        if(acquirePeriod!=null&&acquirePeriod>0){
            this.GET_LOCK_WAIT_TIMEOUT=acquirePeriod.longValue();
        }
    }

    /**
     * 加锁
     */
    public void lock() {
        try {
            sema.acquire();
        } catch (InterruptedException e) {
        }
        for (; ; ) {
            try {
                Long newLockValue = GlobalLockValue.newValue();
                Boolean result = GlobalLockLuaExecuter.acquireLock(redis, globalKey,newLockValue , expired);
                if (Boolean.TRUE.equals(result)) {// 加锁成功
                    GlobalLockValue.set(newLockValue);
                    break;
                }
            } catch (Exception e) {
            }
            try {
                sema.tryAcquire(GET_LOCK_WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {

            }
        }
    }


    /**
     * 尝试加锁（默认超时时间:TYR_LOCK_TIMEOUT）
     */
    public boolean tryLock() {
        return tryLock(TYR_LOCK_TIMEOUT);
    }

    /**
     * 尝试加锁
     * @param timeoutMillis 超时时间
     * @return
     */
    public boolean tryLock(long timeoutMillis) {
        boolean result=false;
        try {
            if (!sema.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS)) {
                return result;
            }
        } catch (InterruptedException e) {
            return result;
        }
        long wait = GET_LOCK_WAIT_TIMEOUT > timeoutMillis ? timeoutMillis : GET_LOCK_WAIT_TIMEOUT;
        int count = 0;
        while ((count += wait) <= timeoutMillis) {
            try {
                Long newLockValue = GlobalLockValue.newValue();
                Boolean rs = GlobalLockLuaExecuter.acquireLock(redis, globalKey, newLockValue, expired);
                if (Boolean.TRUE.equals(rs)) {// 加锁成功
                    GlobalLockValue.set(newLockValue);
                    return true;
                }
            } catch (Exception e) {
                return result;
            }
            try {
                Thread.sleep(wait);
            } catch (InterruptedException e) {
                return result;
            }
        }
        return result;
    }

    /**
     * 解锁
     */
    public void unlock() {
        try {
            Long lockValue = GlobalLockValue.get();
            if (lockValue != null) {
                GlobalLockLuaExecuter.unlock(redis, globalKey, lockValue);
            }
        } catch (Exception e) {
        } finally {
            synchronized (sema) {
                if (sema.availablePermits() < MAX_PERMITS) {
                    sema.release();
                }
            }
            //重置锁的随机值
            GlobalLockValue.reset();
        }
    }

}